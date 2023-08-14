package org.mgwa.w40k.pairing;

import org.mgwa.w40k.pairing.api.AppServer;
import org.mgwa.w40k.pairing.state.AppState;
import org.mgwa.w40k.pairing.util.LoggerSupplier;
import org.mgwa.w40k.pairing.gui.AppWindow;
import org.mgwa.w40k.pairing.web.WebAppUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Dedicated class as the main program entrypoint.
 *
 * <a href="https://stackoverflow.com/questions/52653836/maven-shade-javafx-runtime-components-are-missing">Here is why</a> we can not use {@link AppWindow}.
 */
public final class Main {

    private Main() {}

    private static final Logger LOGGER = LoggerSupplier.INSTANCE.getLogger();

    private static final int DEFAULT_SERVER_PORT = 8000;

    private static IllegalArgumentException handleInputError(String message) {
        LOGGER.severe(message);
        System.exit(1); // This is the end!
        return new IllegalArgumentException(message); // We cheat with the java compiler ;)
    }

    private static Path getMatrixPath(String argument) {
        try {
            return InputUtils.checkReadablePath(argument);
        }
        catch (IllegalArgumentException iae) {
            throw handleInputError(iae.getMessage());
        }
    }

    // Check and auto-setup of server configuration file
    private static void prepareServerConfiguration(String serverConfigurationLocation) {
        Path serverConfigurationPath = Path.of(serverConfigurationLocation);
        File serverConfigurationFile = serverConfigurationPath.toFile();
        if (!serverConfigurationFile.isFile() || !serverConfigurationFile.canRead()) {
            LOGGER.info("Using default server configuration");
            try {
                InputUtils.extractResource("/server.yml", serverConfigurationPath);
            }
            catch (IllegalArgumentException iae) {
                throw handleInputError(iae.getMessage());
            }
            catch (Throwable throwable) {
                LOGGER.severe("Unable to use default server configuration");
                throw new IllegalStateException("Unable to use default server configuration", throwable);
            }
        }
    }

    private static void startServer(String serverConfigurationLocation, AppServer server) {
        try {
            server.run("server", serverConfigurationLocation);
        }
        catch (Exception e) {
            LOGGER.severe("Unable to start the server (" + e.getMessage() + ")");
            throw new IllegalStateException("Internal error", e);
        }
    }

    private static void stopServer(AppServer server) {
        try {
            server.stop();
        } catch (Exception e) {
            LOGGER.severe("Unable to stop the server");
            throw new IllegalStateException(e);
        }
    }

    private static void prepareWebApp(Path webAppTargetFolder) {
        // Preparing target
        if (!Files.exists(webAppTargetFolder)) {
            try {
                Files.createDirectories(webAppTargetFolder);
            }
            catch (IOException e) {
                throw handleInputError(String.format("Unable to create %s directory", webAppTargetFolder));
            }
        }
        else if (!Files.isDirectory(webAppTargetFolder)) {
            throw handleInputError(String.format("%s is not a directory", webAppTargetFolder));
        }
        // Listing web-app resources
        Path meta_inf = Paths.get("META-INF");
        List<Path> resources = InputUtils.listResourcesFromClassPath((source, resourceName) -> {
            if (Objects.requireNonNull(source) != ResourceResolver.Source.JAVA_ARCHIVE) {
                return false;
            }
            if (resourceName.getName(0).equals(meta_inf)) {
                return false;
            }
            Path parent = resourceName.getParent();
            if (parent == null) {
                return false;
            }
            Path lastFolder = parent.getFileName();
            return lastFolder.equals(WebAppUtils.WEB_APP_FILES_FOLDER);
        });
        if (resources.isEmpty()) {
            throw new IllegalStateException("No web-app resource found");
        }
        // Copying resources outside the JAR
        resources.forEach(resource -> {
            Path targetPath = webAppTargetFolder.resolve(resource);
            LOGGER.info(String.format("Extracting %s", targetPath));
            String resourcePath = !File.separator.equals("/")
                ? resource.toString().replaceAll(File.separator, "/")
                : resource.toString();
            InputUtils.extractResource(resourcePath, targetPath);
        });
    }

    private static void openWebApp(Path webAppTargetFolder, int localPort) {
        if (localPort < 0) {
            LOGGER.warning(String.format("Unknown server port ! using default %d", DEFAULT_SERVER_PORT));
            localPort = DEFAULT_SERVER_PORT;
        }
        LOGGER.info(String.format("Starting web-application using server port %d", localPort));
        Path indexPage = webAppTargetFolder.resolve(WebAppUtils.WEB_APP_FILES_FOLDER).resolve("index.html");
        WebAppUtils.openURI(Main.class, LOGGER, indexPage, Integer.toString(localPort));
    }

    private static void cleanUpWebApp(Path webAppTargetFolder) {
        LOGGER.info(String.format("Removing %s", webAppTargetFolder));
        InputUtils.deleteDirectoryRecursively(webAppTargetFolder);
    }

    private static Path prepareLocalDirectory() {
        String userHomeFolder = System.getProperty("user.home");
        Path localFolder = Paths.get(userHomeFolder)
            .resolve(".cache").resolve("mgwa").resolve("pairing");
        LOGGER.info("Using local directory " + localFolder);
        if (!Files.exists(localFolder)) {
            try {
                Files.createDirectories(localFolder);
            }
            catch (IOException ioe) {
                throw handleInputError("Impossible to create " + localFolder);
            }
        }
        return localFolder;
    }

    public static void main(String[] args) {

        // Initialize the application state
        AppState state = new AppState();
        InputUtils.getArgumentAt(args, 0)
            .map(Main::getMatrixPath)
            .ifPresent(state::setMatrixFilePath);

        // Prepare local cache directory
        Path appDir = prepareLocalDirectory();

        // Preparing web pages
        Path webAppTargetFolder = InputUtils.getArgumentAt(args, 2)
            .or(() -> Optional.ofNullable(System.getenv("WEBAPP_TMP_FOLDER")))
            .map(Paths::get)
            .orElseGet(() -> appDir.resolve("web-app"));
        prepareWebApp(webAppTargetFolder);
        try {

            // Preparing the HTTP server
            String serverConfigurationPath = InputUtils.getArgumentAt(args, 1)
                    .or(() -> Optional.ofNullable(System.getenv("API_SERVER_FILE")))
                    .orElse(appDir.resolve("server.yml").toString());
            prepareServerConfiguration(serverConfigurationPath);
            AppServer server = new AppServer(state);

            // Launching the user interface
            AppWindow.launch(state,
                () -> startServer(serverConfigurationPath, server),           // On init
                () -> openWebApp(webAppTargetFolder, server.getServerPort()), // On next
                () -> stopServer(server)                                      // On stop
            );
        }
        finally {
            cleanUpWebApp(webAppTargetFolder);
        }
    }

}
