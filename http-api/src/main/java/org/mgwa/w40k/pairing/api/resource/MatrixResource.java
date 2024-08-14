package org.mgwa.w40k.pairing.api.resource;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.mgwa.w40k.pairing.Army;
import org.mgwa.w40k.pairing.api.model.SetupOverview;
import org.mgwa.w40k.pairing.api.service.MatrixUpdateService;
import org.mgwa.w40k.pairing.api.service.PairingService;
import org.mgwa.w40k.pairing.api.model.EstimatedScore;
import org.mgwa.w40k.pairing.api.model.Match;
import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.Score;
import org.mgwa.w40k.pairing.state.AppState;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.InputStream;
import java.util.List;

@Path("")
public class MatrixResource {

    public static final Score DEFAULT_SCORE = Score.newDefault();

    public MatrixResource(AppState state, PairingService service, MatrixUpdateService matrixUpdateService) {
        this.state = state;
        this.service = service;
        this.matrixUpdateService = matrixUpdateService;
    }

    private final AppState state;
    private final PairingService service;
    private final MatrixUpdateService matrixUpdateService;

    private Match getMatchFromState() {
        return new Match(
                state.getRowTeamName(),
                state.getColTeamName(),
                state.getArmyCount());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("match")
    public Match getMatch() {
        return getMatchFromState();
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
        List<List<EstimatedScore>> scores = SetupOverview.getScoresFromMatrix(service.getMatrix(), state.getArmyCount(), EstimatedScore.from(DEFAULT_SCORE));
        return Response.ok(scores, MediaType.APPLICATION_JSON_TYPE).build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("reset")
    public Response resetMatrix(
            @FormDataParam("rows_team") String rowsTeamName,
            @FormDataParam("cols_team") String columnsTeamName,
            @FormDataParam("size")      Integer armyCount,
            @FormDataParam("file")      FormDataContentDisposition contentDisposition,
            @FormDataParam("file")      InputStream inputStream
    ) {
        matrixUpdateService.setTeamNames(rowsTeamName, columnsTeamName);
        Matrix matrix = matrixUpdateService.setupMatrix(contentDisposition, inputStream, armyCount, DEFAULT_SCORE);
        Match match = getMatchFromState(); // Needs to be called after the reset
        SetupOverview overview = SetupOverview.from(match, matrix, EstimatedScore.from(DEFAULT_SCORE));
        return Response.ok(overview, MediaType.APPLICATION_JSON_TYPE).build();
    }

}
