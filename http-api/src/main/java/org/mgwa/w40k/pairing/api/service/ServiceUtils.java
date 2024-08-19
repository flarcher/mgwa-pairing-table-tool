package org.mgwa.w40k.pairing.api.service;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Utility class for services.
 */
class ServiceUtils {
    private ServiceUtils() {}

    static RuntimeException internalError(String message) {
        return new WebApplicationException(message, Response.Status.INTERNAL_SERVER_ERROR);
    }

    static RuntimeException badRequest(String message) {
        return new WebApplicationException(message, Response.Status.BAD_REQUEST);
    }

    static boolean hasFileAttached(FormDataContentDisposition contentDisposition) {
        // Info: contentDisposition.getSize() is always -1L
        return contentDisposition != null && ! contentDisposition.getFileName().isBlank();
    }

    @Nonnull
    static String trimName(String name) {
        return Optional.ofNullable(name).orElse("").trim();
    }

    @Nonnull
    private static String getFileNameExtension(String fullFileName) {
        return Optional.ofNullable(fullFileName)
                .map(ffn -> {
                    int indexOfDot = ffn.lastIndexOf('.');
                    return ffn.substring(indexOfDot + 1);
                })
                .orElse("");
    }

    static Optional<FileExtensionSupport> getFileSupport(String fullFileName) {
        return Stream.of(FileExtensionSupport.values())
                .filter(fes -> fes.match(getFileNameExtension(fullFileName)))
                .findFirst();
    }

}
