package org.mgwa.w40k.pairing;

import org.mgwa.w40k.pairing.state.AppState;
import org.mgwa.w40k.pairing.util.LoggerSupplier;
import org.mgwa.w40k.pairing.gui.AppWindow;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Dedicated class as the main program entrypoint.
 * 
 * Here is why we do not use {@link AppWindow}: https://stackoverflow.com/questions/52653836/maven-shade-javafx-runtime-components-are-missing
 */
public final class Main {

    private Main() {}

    private static IllegalArgumentException handleInputError(String message) {
        LoggerSupplier.INSTANCE.getLogger().severe(message);
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

    public static void main(String[] args) {

        // Get the application state
        AppState state = AppState.INSTANCE;

        // Reading the optional file path in arguments
        Optional.of(args)
            .filter(arguments -> arguments.length > 0)
            .map(arguments -> getMatrixPath(arguments[0]))
            .ifPresent(state::setMatrixFilePath);

        // Launching the user interface
        AppWindow.main();
    }

}
