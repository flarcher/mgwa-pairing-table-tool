package org.mgwa.w40k.pairing.api.resource;

import jakarta.ws.rs.Produces;
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
import org.mgwa.w40k.pairing.matrix.Matrix;

import java.util.List;
import java.util.Objects;

@Path("analysis")
public class AnalysisResource {

    public AnalysisResource(PairingService service) {
        this.service = service;
    }

    private final PairingService service;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("")
    public Response guidePairing(@NotNull @Valid PairingRequest request) {
        Matrix matrix = Objects.requireNonNull(null); // TODO
        List<PairingResponseItem> analysisResponse = service.estimatePairing(matrix, request);
        return Response.ok(analysisResponse, MediaType.APPLICATION_JSON_TYPE).build();
    }
}
