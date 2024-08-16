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
import java.util.Optional;

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

    private static boolean isRow(String rowsOrColumn) {
        switch (rowsOrColumn) {
            case "rows", "row" -> {
                return true;
            }
            case "columns", "column", "col", "cols" -> {
                return false;
            }
            default ->
                    throw new WebApplicationException(
                            "Unknown kind of army " + rowsOrColumn,
                            Response.Status.BAD_REQUEST);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("match/{rowsOrColumns}")
    public Response getArmies(@PathParam("rowsOrColumns") String rowsOrColumns) {
        Matrix matrix = service.getMatrix();
        boolean isRow = isRow(rowsOrColumns);
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

    private static int expectUnsignedInteger(String value) {
        try {
            return Integer.parseUnsignedInt(value);
        } catch (NumberFormatException nfe) {
            throw new WebApplicationException("Expected an integer, got <"+value+">", Response.Status.BAD_REQUEST);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("update/{row}/{column}")
    public Response updateScore(
            @PathParam("row") String rowIndex, @PathParam("column") String columnIndex,
            @FormParam("minimum") String minimum, @FormParam("maximum") String maximum
    ) {
        Score rq = Score.of(expectUnsignedInteger(minimum), expectUnsignedInteger(maximum));
        matrixUpdateService.updateScore(expectUnsignedInteger(rowIndex), expectUnsignedInteger(columnIndex), rq);
        EstimatedScore rs = EstimatedScore.from(rq);
        return Response.ok(rs, MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("match/{rowsOrColumns}/army")
    public Response updateTeam(
            @PathParam("rowsOrColumns") String rowsOrColumns,
            @FormParam("name") String newName) {
        boolean isRow = isRow(rowsOrColumns);
        matrixUpdateService.updateTeamName(isRow, newName);
        return Response.ok(getMatchFromState(), MediaType.APPLICATION_JSON_TYPE).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("army/{rowsOrColumns}/{index}")
    public Response updateArmy(
            @PathParam("rowsOrColumns") String rowsOrColumns,
            @FormParam("index") int index,
            @FormParam("name") String newName) {
        boolean isRow = isRow(rowsOrColumns);
        Optional<Army> army = matrixUpdateService.updateArmyName(isRow, index, newName);
        return army
            .map(a -> Response.ok(a, MediaType.APPLICATION_JSON).build())
            .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

}
