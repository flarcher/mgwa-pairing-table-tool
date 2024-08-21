package org.mgwa.w40k.pairing.api.resource;

import jakarta.ws.rs.core.StreamingOutput;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.mgwa.w40k.pairing.Army;
import org.mgwa.w40k.pairing.api.model.SetupOverview;
import org.mgwa.w40k.pairing.api.service.FileExtensionSupport;
import org.mgwa.w40k.pairing.api.service.MatrixService;
import org.mgwa.w40k.pairing.api.service.PairingService;
import org.mgwa.w40k.pairing.api.model.EstimatedScore;
import org.mgwa.w40k.pairing.api.model.Match;
import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.Score;
import org.mgwa.w40k.pairing.state.AppState;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

@Path("")
public class MatrixResource {

    private static final Score DEFAULT_SCORE = Score.newDefault();
    private static final String DEFAULT_FILENAME = "matrix";

    public MatrixResource(AppState state, PairingService pairingService, MatrixService matrixService) {
        this.state = state;
        this.pairingService = pairingService;
        this.matrixService = matrixService;
    }

    private final AppState state;
    private final PairingService pairingService;
    private final MatrixService matrixService;

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
        Matrix matrix = pairingService.getMatrix();
        boolean isRow = WebInputUtils.isRow(rowsOrColumns);
        List<String> armyNames = matrix.getArmies(isRow).stream().map(Army::getName).toList();
        return Response.ok(armyNames, MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("scores")
    public Response getDefault() {
        List<List<EstimatedScore>> scores = SetupOverview.getScoresFromMatrix(pairingService.getMatrix(), state.getArmyCount(), EstimatedScore.from(DEFAULT_SCORE));
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
            @FormDataParam("minimum")   String minimum,
            @FormDataParam("maximum")   String maximum,
            @FormDataParam("file")      FormDataContentDisposition contentDisposition,
            @FormDataParam("file")      InputStream inputStream
    ) {
        Score defaultScore = WebInputUtils.considerScore(minimum, maximum, DEFAULT_SCORE);
        matrixService.setTeamNames(rowsTeamName, columnsTeamName);
        Matrix matrix = matrixService.setupMatrix(contentDisposition, inputStream, armyCount, defaultScore);
        Match match = getMatchFromState(); // Needs to be called after the reset
        SetupOverview overview = SetupOverview.from(match, matrix, EstimatedScore.from(defaultScore));
        return Response.ok(overview, MediaType.APPLICATION_JSON_TYPE).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("update/{row}/{column}")
    public Response updateScore(
            @PathParam("row") String rowIndex, @PathParam("column") String columnIndex,
            @FormParam("minimum") String minimum, @FormParam("maximum") String maximum
    ) {
        Score rq = Score.of(WebInputUtils.expectUnsignedInteger(minimum), WebInputUtils.expectUnsignedInteger(maximum));
        matrixService.updateScore(WebInputUtils.expectUnsignedInteger(rowIndex), WebInputUtils.expectUnsignedInteger(columnIndex), rq);
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
        boolean isRow = WebInputUtils.isRow(rowsOrColumns);
        matrixService.updateTeamName(isRow, newName);
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
        boolean isRow = WebInputUtils.isRow(rowsOrColumns);
        Optional<Army> army = matrixService.updateArmyName(isRow, index, newName);
        return army
            .map(a -> Response.ok(a, MediaType.APPLICATION_JSON).build())
            .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    private Response writeMatrix(FileExtensionSupport support) {
        StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                matrixService.writeExcelFile(outputStream, support);
            }
        };
        return Response.ok(streamingOutput, MediaType.APPLICATION_OCTET_STREAM)
                //.header("content-type", "application/vnd.ms-excel")
                .header("content-disposition","attachment; filename = " + DEFAULT_FILENAME + "." + support.getDefaultExtension())
                .build();
    }

    @GET
    @Path("download/xlsx")
    public Response downloadMatrixAsXLS() {
        return writeMatrix(FileExtensionSupport.EXCEL_SPREADSHEET);
    }

    @GET
    @Path("download/json")
    public Response downloadMatrixAsJSON() {
        return writeMatrix(FileExtensionSupport.JSON);
    }

}
