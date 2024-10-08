package org.mgwa.w40k.pairing.api.resource;

import org.mgwa.w40k.pairing.api.model.AppStatus;
import org.mgwa.w40k.pairing.api.service.StatusService;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

/**
 * Handles the lifecycle of the application.
 */
@Path("app")
public class AppResource {

    private static final String STATUS_HTTP_HEADER = "X-Status";

    public AppResource(StatusService statusService) {
        this.statusService = statusService;
    }

    private final StatusService statusService;

    /**
     * Application layer health check
     * @return Always 200
     */
    @GET
    @Path("alive")
    public Response isAlive() {
        AppStatus status = statusService.getStatus();
        Response.ResponseBuilder builder = status != AppStatus.EXITING
            ? Response.ok()           // Running (or will be soon)
            : Response.serverError(); // Stopping/stopped
        builder.header("Access-Control-Expose-Headers", STATUS_HTTP_HEADER);
        builder.header(STATUS_HTTP_HEADER, status.name().toLowerCase());
        return builder.build();
    }

    /**
     * Exits the application and quits the API
     */
    @POST
    @Path("exit")
    public Response exit() {
        return statusService.exiting()
            ? Response.ok().build() // Stopping
            : Response.serverError().build(); // Already stopping/stopped
    }

}
