package org.mgwa.w40k.pairing.api.service;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

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
}
