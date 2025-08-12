package org.mgwa.w40k.pairing.api.resource;

import jakarta.ws.rs.core.StreamingOutput;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.mgwa.w40k.pairing.api.model.SetupOverview;
import org.mgwa.w40k.pairing.api.service.FileExtensionSupport;
import org.mgwa.w40k.pairing.api.service.MatrixService;
import org.mgwa.w40k.pairing.api.model.EstimatedScore;
import org.mgwa.w40k.pairing.api.model.Match;
import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.Score;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Path("")
public class MatrixResource {

    private static final Score DEFAULT_SCORE = Score.newDefault();
    private static final String DEFAULT_FILENAME = "matrix";

    public MatrixResource(MatrixService matrixService) {
        this.matrixService = matrixService;
    }

    private final MatrixService matrixService;

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
        matrixService.checkTeamNames(rowsTeamName, columnsTeamName);
        Matrix matrix = matrixService.setupMatrix(contentDisposition, inputStream, armyCount, defaultScore);
        Match match = new Match(rowsTeamName, columnsTeamName, armyCount);
        SetupOverview overview = SetupOverview.from(match, matrix, EstimatedScore.from(defaultScore));
        return Response.ok(overview, MediaType.APPLICATION_JSON_TYPE).build();
    }

    private Response writeMatrix(Matrix matrix, FileExtensionSupport support) {
        StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                matrixService.writeExcelFile(matrix, outputStream, support);
            }
        };
        return Response.ok(streamingOutput, MediaType.APPLICATION_OCTET_STREAM)
                //.header("content-type", "application/vnd.ms-excel")
                .header("content-disposition","attachment; filename = " + DEFAULT_FILENAME + "." + support.getDefaultExtension())
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("download/xlsx")
    public Response downloadMatrixAsXLS(SetupOverview input) {
        return writeMatrix(input.getMatrix(), FileExtensionSupport.EXCEL_SPREADSHEET);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("download/json")
    public Response downloadMatrixAsJSON(SetupOverview input) {
        return writeMatrix(input.getMatrix(), FileExtensionSupport.JSON);
    }

}
