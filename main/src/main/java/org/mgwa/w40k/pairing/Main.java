package org.mgwa.w40k.pairing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.mgwa.w40k.pairing.api.AppServer;
import org.mgwa.w40k.pairing.util.LoggerSupplier;
import org.mgwa.w40k.pairing.web.WebAppUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dedicated class as the main program entrypoint.
 *
 * <a href="https://stackoverflow.com/questions/52653836/maven-shade-javafx-runtime-components-are-missing">Here is why</a> we can not use {@link AppWindow}.
 */
public final class Main {

    private Main() {}

    private static final Logger LOGGER = LoggerSupplier.INSTANCE.getLogger();

    private static void cleanUpLogFile() {
        try {
            Files.deleteIfExists(LoggerSupplier.INSTANCE.getLogFilePath());
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

    private static final int DEFAULT_SERVER_PORT = 8000;

    private static IllegalArgumentException handleInputError(String message) {
        LOGGER.severe(message);
        System.exit(1); // This is the end!
        return new IllegalArgumentException(message); // We cheat with the java compiler ;)
    }

    // Check and auto-setup of server configuration file
    private static void prepareServerConfiguration(Path serverConfigurationPath) {
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

    private static void startServer(Path serverConfigurationLocation, AppServer server) {
        try {
            server.run("server", serverConfigurationLocation.toString());
        }
        catch (Exception e) {
            LOGGER.severe("Unable to start the server (" + e.getMessage() + ")");
            throw new IllegalStateException("Internal error", e);
        }
    }

    private static void stopServer(AppServer server) {
        try {
            LOGGER.info("Stopping the server");
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
        int apiPort;
        if (localPort < 0) {
            LOGGER.warning(String.format("Unknown server port ! using default %d", DEFAULT_SERVER_PORT));
            apiPort = DEFAULT_SERVER_PORT;
        }
        else {
            apiPort = localPort;
        }
        LOGGER.info(String.format("Starting web-application using server port %d", apiPort));
        Path indexPage = webAppTargetFolder.resolve(WebAppUtils.WEB_APP_FILES_FOLDER).resolve("index.html");
        // Concurrency fix:
        // See https://stackoverflow.com/questions/23176624/javafx-freeze-on-desktop-openfile-desktop-browseuri
        Executors.defaultThreadFactory().newThread(() -> {
            try {
                WebAppUtils.openURI(Main.class, LOGGER, indexPage, Integer.toString(apiPort));
            }
            catch (Throwable t) {
                LOGGER.severe("Error trying to open web page " + indexPage);
                throw t;
            }
        }).start();
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

    private static Optional<Integer> readPortFromServerYamlFile(Path serverYamlFile) {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JsonNode rootNode;
        try (InputStream is = new BufferedInputStream(
                Files.newInputStream(serverYamlFile, StandardOpenOption.READ))) {
            rootNode = mapper.readTree(is);
        }
        catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "Unable to parse " + serverYamlFile, ioe);
            return Optional.empty();
        }
        return               Optional.ofNullable(rootNode.get("server"))
                .flatMap(node -> Optional.ofNullable(node.get("connector")))
                .flatMap(node -> Optional.ofNullable(node.get("port")))
                .flatMap(field -> {
                    if (field.getNodeType() == JsonNodeType.NUMBER) {
                        return Optional.of(field.asInt(-1));
                    } else {
                        return Optional.empty();
                    }
                });
    }

    public static void main(String[] args) {

        // Prepare local cache directory
        Path appDir = prepareLocalDirectory();

        // Preparing web pages
        Path webAppTargetFolder = InputUtils.getArgumentAt(args, 2)
            .or(() -> Optional.ofNullable(System.getenv("WEBAPP_TMP_FOLDER")))
            .map(Paths::get)
            .orElse(appDir);
        prepareWebApp(webAppTargetFolder);

        // Preparing the HTTP server
        Path serverConfigurationPath = InputUtils.getArgumentAt(args, 1)
                .or(() -> Optional.ofNullable(System.getenv("API_SERVER_FILE")))
                .map(Path::of)
                .orElse(appDir.resolve("server.yml"));
        prepareServerConfiguration(serverConfigurationPath);

        // Starting the Web-App if possible
        Optional<Integer> readServerPort = readPortFromServerYamlFile(serverConfigurationPath);
        readServerPort.ifPresent(port -> {
            LOGGER.info("Starting Web-app early with API port " + port);
            openWebApp(webAppTargetFolder, port);
        });

        // Start the server API
        AppServer server = new AppServer(() -> {
            // Cleans the web-app files up
            cleanUpWebApp(webAppTargetFolder);
            // Logs cleaning
            cleanUpLogFile();
            // Done
            LOGGER.info("Stopped");
        });
        startServer(serverConfigurationPath, server);

        // Starting the Web-App, if not done already
        if (readServerPort.isPresent()) {
            if (server.getServerPort() != readServerPort.get()) {
                LOGGER.severe(String.format("Read port %d is not the API port %d", server.getServerPort(), readServerPort.get()));
            }
        }
        else {
            openWebApp(webAppTargetFolder, server.getServerPort());
        }
    }

}
