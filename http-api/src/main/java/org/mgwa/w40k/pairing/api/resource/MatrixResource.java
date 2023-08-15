package org.mgwa.w40k.pairing.api.resource;

import org.mgwa.w40k.pairing.api.model.Match;
import org.mgwa.w40k.pairing.api.model.Matrix;
import org.mgwa.w40k.pairing.state.AppState;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Objects;

@Path("")
public class MatrixResource {

    public MatrixResource(AppState state) {
        this.state = Objects.requireNonNull(state);
    }

    private final AppState state;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("match")
    public Match getMatch() {
        return new Match(
                state.getRowTeamName(),
                state.getColTeamName(),
                state.getArmyCount());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("matrix")
    public Matrix getDefault() {
        // TODO
        return new Matrix();
    }
}
