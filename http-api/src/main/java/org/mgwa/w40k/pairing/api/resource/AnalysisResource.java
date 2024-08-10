package org.mgwa.w40k.pairing.api.resource;

import org.mgwa.w40k.pairing.api.model.PairingResponseItem;
import org.mgwa.w40k.pairing.api.service.PairingService;
import org.mgwa.w40k.pairing.api.model.PairingRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("analysis")
public class AnalysisResource {

    public AnalysisResource(PairingService service) {
        this.service = service;
    }

    private final PairingService service;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("guide")
    public Response guidePairing(@NotNull @Valid PairingRequest request) {
        List<PairingResponseItem> analysisResponse = service.estimatePairing(request);
        return Response.ok(analysisResponse, MediaType.APPLICATION_JSON_TYPE).build();
    }
}
