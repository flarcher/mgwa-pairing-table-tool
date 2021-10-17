package org.mgwa.w40k.pairing.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Dedicated class as the main program entrypoint.
 * 
 * Here is why we do not use {@link AppWindow}: https://stackoverflow.com/questions/52653836/maven-shade-javafx-runtime-components-are-missing
 */
public final class Main {

	private Main() {}

    public static void main(String[] args) {
        AppWindow.main();
    }

}
