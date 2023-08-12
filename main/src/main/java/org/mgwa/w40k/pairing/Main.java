package org.mgwa.w40k.pairing;

import org.mgwa.w40k.pairing.api.AppServer;
import org.mgwa.w40k.pairing.state.AppState;
import org.mgwa.w40k.pairing.util.LoggerSupplier;
import org.mgwa.w40k.pairing.gui.AppWindow;

import java.io.File;
import java.io.InputStream;
import java.nio.file.*;
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

    private static IllegalArgumentException handleInputError(String message) {
        LOGGER.severe(message);
        System.exit(1); // This is the end!
        return new IllegalArgumentException(message); // We cheat with the java compiler ;)
    }

    private static Path getMatrixPath(String argument) {
        try {
            Path filePath = Paths.get(argument);
            File file = filePath.toFile();
            if (file.isFile() && file.canRead()) {
                return filePath;
            }
            else {
                throw handleInputError(String.format("The path %s is not a file or can not be read", filePath));
            }
        }
        catch (InvalidPathException ipe) {
            throw handleInputError(String.format("The argument %s is an invalid path", argument));
        }
    }

    /**
     * @param args All arguments
     * @param index Zero-based index
     * @return The argument if any.
     */
    private static Optional<String> getArgumentAt(String[] args, int index) {
        return args.length > index ? Optional.of(args[index]) : Optional.empty();
    }

    private static void startServer(String serverConfigurationLocation, AppServer server) {

        // Check and auto-setup of server configuration file
        Path serverConfigurationPath = Path.of(serverConfigurationLocation);
        File serverConfigurationFile = serverConfigurationPath.toFile();
        if (!serverConfigurationFile.isFile() || !serverConfigurationFile.canRead()) {
            if (serverConfigurationFile.exists()) {
                throw handleInputError(String.format("The server configuration file %s can not be read", serverConfigurationLocation));
            }
            else {
                LOGGER.info("Using default server configuration");
                try (InputStream serverFileInputStream = Main.class.getResourceAsStream("/server.yml")) {
                    Files.copy(Objects.requireNonNull(serverFileInputStream), serverConfigurationPath);
                }
                catch (Throwable throwable) {
                    LOGGER.severe("Unable to use default server configuration");
                    throw new IllegalStateException("Unable to use default server configuration", throwable);
                }
            }
        }

        // Start
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

    public static void main(String[] args) {

        // Initialize the application state
        AppState state = new AppState();
        getArgumentAt(args, 0)
            .map(Main::getMatrixPath)
            .ifPresent(state::setMatrixFilePath);

        // Preparing the HTTP server
        String serverConfigurationPath = getArgumentAt(args, 1)
            .or(() -> Optional.ofNullable(System.getenv("API_SERVER_FILE")))
            .orElse(System.getProperty("user.dir") + File.separator + "server.yml");
        AppServer server = new AppServer(state);

        // Launching the user interface
        AppWindow.launch(state,
            () -> { startServer(serverConfigurationPath, server); }, // On init
            () -> { stopServer(server); } // On stop
        );
    }

}
