package org.mgwa.w40k.pairing.api.resource;

import org.mgwa.w40k.pairing.Army;
import org.mgwa.w40k.pairing.api.service.PairingService;
import org.mgwa.w40k.pairing.api.model.EstimatedScore;
import org.mgwa.w40k.pairing.api.model.Match;
import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.Score;
import org.mgwa.w40k.pairing.state.AppState;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.IntStream;

@Path("")
public class MatrixResource {

    public MatrixResource(AppState state, PairingService service) {
        this.state = state;
        this.service = service;
    }

    private final AppState state;
    private final PairingService service;

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
    @Path("match/{rowsOrColumns}")
    public Response getArmies(@PathParam("rowsOrColumns") String rowsOrColumns) {
        Matrix matrix = service.getMatrix();
        boolean isRow;
        switch (rowsOrColumns) {
            case "rows", "row" -> {
                isRow = true;
            }
            case "columns", "column", "col", "cols" -> {
                isRow = false;
            }
            default ->
                throw new WebApplicationException(
                    "Unknown kind of army " + rowsOrColumns,
                    Response.Status.BAD_REQUEST);
        }
        List<String> armyNames = matrix.getArmies(isRow).stream().map(Army::getName).toList();
        return Response.ok(armyNames, MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("scores")
    public Response getDefault() {
        Matrix matrix = service.getMatrix();
        EstimatedScore DEFAULT_SCORE = EstimatedScore.from(Score.newDefault());
        int size = state.getArmyCount();
        List<List<EstimatedScore>> scores = IntStream.range(0, size)
                .mapToObj(row ->
                    IntStream.range(0, size)
                        .mapToObj(column -> matrix.getScore(row, column)
                            .map(EstimatedScore::from)
                            .orElse(DEFAULT_SCORE))
                        .toList()
                )
                .toList();
        return Response.ok(scores, MediaType.APPLICATION_JSON_TYPE).build();
    }
}
