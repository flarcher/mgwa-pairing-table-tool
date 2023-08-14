package org.mgwa.w40k.pairing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.CodeSource;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class InputUtils {

    private InputUtils() {} // Utility class

    static Path checkReadablePath(String argument) {
        try {
            Path filePath = Paths.get(argument);
            File file = filePath.toFile();
            if (file.isFile() && file.canRead()) {
                return filePath;
            }
            else {
                throw new IllegalArgumentException(String.format("The path %s is not a file or can not be read", filePath));
            }
        }
        catch (InvalidPathException ipe) {
            throw new IllegalArgumentException(String.format("The argument %s is an invalid path", argument));
        }
    }

    /**
     * @param args All arguments
     * @param index Zero-based index
     * @return The argument if any.
     */
    static Optional<String> getArgumentAt(String[] args, int index) {
        return args.length > index ? Optional.of(args[index]) : Optional.empty();
    }

    /**
     * Copy the content of a JAR file on the host file system.
     * @param inputResourcePath Path of the resource inside the JAR
     * @param outputFilePath File path for the target file
     */
    static void extractResource(String inputResourcePath, Path outputFilePath) {
        // Create tree
        Path outputFolder = outputFilePath.getParent();
        if (!Files.exists(outputFolder)) {
            try {
                Files.createDirectories(outputFolder);
            } catch (IOException ioException) {
                throw new IllegalStateException(
                        String.format("Unable to create directory %s", outputFolder),
                        ioException);
            }
        }
        // Make the resource path absolute
        String resourcePath = !inputResourcePath.startsWith("/") ? "/" + inputResourcePath : inputResourcePath;
        // Copy
        try (InputStream serverFileInputStream = InputUtils.class.getResourceAsStream(resourcePath)) {
            if (serverFileInputStream == null) {
                throw new IllegalArgumentException(String.format("Unable to locate resource %s", resourcePath));
            }
            else {
                try {
                    Files.copy(serverFileInputStream, outputFilePath, StandardCopyOption.REPLACE_EXISTING);
                }
                catch (IOException ioException) {
                    throw new IllegalStateException(
                            String.format("Unable to extract %s as %s", resourcePath, outputFilePath),
                            ioException);
                }
            }
        }
        catch (IOException ioException) {
            throw new IllegalStateException(
                    String.format("Unable to close %s", resourcePath),
                    ioException);
        }
    }

    static List<Path> listResourcesFromClassPath(BiPredicate<ResourceResolver.Source, Path> predicate) {
        List<Path> list = new ArrayList<>();
        try {
            ResourceResolver.getResources((source, rsc) -> {
                if (predicate.test(source, rsc)) {
                    list.add(rsc);
                }
            });
        }
        catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
        return list;
    }

    /**
     * @param resourceFolder Resource folder.
     * @return A list of all resource files (of the JAR) inside the folder.
     */
    static List<String> listResourcesFromJar(String resourceFolder) {
        Objects.requireNonNull(resourceFolder);
        CodeSource src = InputUtils.class.getProtectionDomain().getCodeSource();
        List<String> list = new ArrayList<String>();
        URL jar = src.getLocation();
        String entryNamePrefix = ! resourceFolder.endsWith("/") ? resourceFolder + "/" : resourceFolder;
        try (ZipInputStream zip = new ZipInputStream( jar.openStream() )) {
            ZipEntry ze = null;
            while ((ze = zip.getNextEntry()) != null) {
                String entryName = ze.getName();
                if (!ze.isDirectory() && entryName.startsWith(entryNamePrefix)) {
                    list.add(entryName);
                }
            }
        }
        catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
        return list;
    }

    static void deleteDirectoryRecursively(Path folderToDelete) {
        try {
            Files.walkFileTree(folderToDelete, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    static Path createTempDirectory(String prefix) {
        try {
            Path tmpFolder = Files.createTempDirectory(prefix);
            return Files.exists(tmpFolder) ? tmpFolder : Files.createDirectory(tmpFolder);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create "+prefix+" temporary folder", e);
        }
    }
}
