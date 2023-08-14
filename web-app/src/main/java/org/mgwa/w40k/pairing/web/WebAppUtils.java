
package org.mgwa.w40k.pairing.web;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class WebAppUtils {

    private WebAppUtils() { } // Utility class

    /**
     * Resource folder that contains the Web-App resources.
     * See related POM configuration.
     */
    public static final Path WEB_APP_FILES_FOLDER = Paths.get("web-app");

    /**
     * Opens a URI within the application JAR file, using OS specific corresponding application.
     * @param clazz Caller's class (in order to identify the class loader)
     * @param logger Logging implementation
     * @param fileName Web page file path.
     * @param hashParameter Optional hash parameter (can be null)
     * @throws IllegalArgumentException In case of a bad file name
     */
    public static void openURI(Class<?> clazz, Logger logger, Path fileName, String hashParameter) {
        // Checks
        Objects.requireNonNull(fileName);
        File file = fileName.toFile();
        if (!file.exists() || !file.canRead()) {
            throw new IllegalArgumentException(String.format("File %s does not exist", fileName));
        }

        // URI building
        URI uri = null;
        try {
            uri = clazz.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path currentPath = Paths.get(uri).getParent();
            if (!currentPath.toFile().isDirectory()) {
                throw new IllegalStateException("Internal error: "+currentPath+" is not a directory");
            }
            uri = currentPath.resolve(fileName).toUri();
            if (hashParameter != null) {
                // The use of a hashParameter param does not work
                // See https://stackoverflow.com/questions/24334436/setting-a-query-string-from-javas-desktop-getdesktop-browseuri-uri
                // new URI("file", "", programPath, "version=1.0.3", "");
                uri = new URI(uri.toString() + "#" + hashParameter);
            }
            logger.info(String.format("Opening URI %s", uri));
            // Opening
            Desktop.getDesktop().browse(uri);
        }
        catch (URISyntaxException e) {
            throw new IllegalStateException("Internal error", e);
        }
        catch (IOException e) {
            logger.severe(String.format("Impossible to open %s", uri));
            throw new IllegalArgumentException("Impossible to open " + uri, e);
        }
    }
}
