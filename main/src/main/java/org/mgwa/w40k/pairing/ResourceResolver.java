package org.mgwa.w40k.pairing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * list resources available from the classpath @ *
 * Inspired by https://stackoverflow.com/a/3923182
 */
public class ResourceResolver {

    private ResourceResolver() {} // Utility class

    public enum Source {
        JAVA_ARCHIVE,
        DIRECTORY
    }

    public static void getResources(BiConsumer<Source, Path> consumer) throws IOException {
        final String classPath = System.getProperty("java.class.path", ".");
        final String[] classPathElements = classPath.split(System.getProperty("path.separator"));
        for (final String element : classPathElements){
            final File file = new File(element);
            if (file.isDirectory()) {
                getResourcesFromDirectory(file, rsc -> consumer.accept(Source.DIRECTORY, rsc));
            } else{
                getResourcesFromJarFile(file, rsc -> consumer.accept(Source.JAVA_ARCHIVE, rsc));
            }
        }
    }

    private static void getResourcesFromJarFile(
            final File file,
            Consumer<Path> consumer) throws IOException {
        try (ZipFile zf = new ZipFile(file)) {
            final Enumeration<? extends ZipEntry> e = zf.entries();
            while (e.hasMoreElements()) {
                final ZipEntry ze = (ZipEntry) e.nextElement();
                if (!ze.isDirectory()) {
                    Path entryPath = pathFromArray(ze.getName().split("/"));
                    consumer.accept(entryPath);
                }
            }
        } catch(final ZipException e){
            throw new IOException(e);
        }
    }

    private static void getResourcesFromDirectory(
            final File directory,
            Consumer<Path> consumer) throws IOException {

        for (final File file : directory.listFiles()) {
            if (file.isDirectory()) {
                getResourcesFromDirectory(file, consumer);
            } else{
                consumer.accept(file.toPath());
            }
        }
    }

    private static Path pathFromArray(String[] items) {
        switch (items.length) {
            case 0 -> { return null; }
            case 1 -> { return Paths.get(items[0]); }
            default -> {
                String[] more = new String[items.length - 1];
                System.arraycopy(items, 1, more, 0, items.length - 1);
                return Paths.get(items[0], more);
            }
        }
    }
}  