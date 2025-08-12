package org.mgwa.w40k.pairing.api.service;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.mgwa.w40k.pairing.Army;
import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.MatrixReader;
import org.mgwa.w40k.pairing.matrix.MatrixWriter;
import org.mgwa.w40k.pairing.matrix.Score;
import org.mgwa.w40k.pairing.util.LoggerSupplier;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Service about score matrix update.
 */
public class MatrixService {

    private static final Logger LOGGER = LoggerSupplier.INSTANCE.getLogger();
    private static final int MIN_ARMY_COUNT = 0;
    private static final int MAX_ARMY_COUNT = 10;
    private static final int MAX_ARMY_NAME_LENGTH = 25;

    public MatrixService() { }

    public void checkTeamNames(String rowsTeamName, String columnsTeamName) {
        String trimedRowsTeamName = ServiceUtils.trimName(rowsTeamName);
        String trimedColsTeamName = ServiceUtils.trimName(columnsTeamName);
        Stream.of(trimedRowsTeamName, trimedColsTeamName).forEach(name -> {
            if (name.isBlank() || name.length() > MAX_ARMY_NAME_LENGTH) {
                throw ServiceUtils.badRequest(String.format("Invalid army name (max length is %d)", MAX_ARMY_NAME_LENGTH));
            }
        });
        if (trimedRowsTeamName.equalsIgnoreCase(trimedColsTeamName)) {
            throw ServiceUtils.badRequest("Team names must be different");
        }
    }

    private Matrix resetMatrix(Integer armyCount, Score defaultScore) {
        // Input checks
        if (armyCount == null || armyCount < MIN_ARMY_COUNT || armyCount > MAX_ARMY_COUNT) {
            throw ServiceUtils.badRequest(String.format("Invalid army count (must be between %d and %d)", MIN_ARMY_COUNT, MAX_ARMY_COUNT));
        }
        Score internalDefaultScore = Optional.ofNullable(defaultScore).orElse(Score.newDefault());

        // State update
        Matrix matrix = loadMatrixDefault(armyCount, internalDefaultScore);
        if (matrix.getSize() != armyCount) {
            throw ServiceUtils.internalError("Inconsistent army count");
        }
        return matrix;
    }

    private static Matrix loadMatrixDefault(int armyCount, Score defaultScore) {
        List<String> names = IntStream.range(0, armyCount)
                .mapToObj(Integer::toString)
                .collect(Collectors.toList());
        Matrix matrix = Matrix.createWithoutScores(
                Army.createArmies(names, true),
                Army.createArmies(names, false));
        return matrix.setDefaultScore(defaultScore);
    }

    private static Matrix readMatrix(InputStream stream, String fileName) {
        FileExtensionSupport support = ServiceUtils.getFileSupport(fileName)
                .orElseThrow(() -> ServiceUtils.badRequest("Unsupported file extension for " + fileName));

        MatrixReader reader;
        try {
            reader = support.getFactory().getReaderBuilder().newReader(new BufferedInputStream(stream));
        }
        catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Unable to read file " + fileName, ioe);
            throw ServiceUtils.badRequest(String.format("Unable to read file %s of format %s", fileName, support));
        }

        try (reader) {
            return reader.read();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, String.format("Unable to parse matrix file %s of format %s", fileName, support), e);
            throw ServiceUtils.badRequest(String.format("Unable to parse file %s of format %s", fileName, support));
        }
    }

    private Matrix uploadMatrix(FormDataContentDisposition contentDisposition, InputStream fileStream) {
        String fileName = contentDisposition.getFileName();
        Objects.requireNonNull(fileName);
        Objects.requireNonNull(fileStream);
        if (fileName.isBlank()) {
            throw ServiceUtils.badRequest("File name is mandatory for upload");
        }
        LOGGER.info(String.format("Reading file %s", fileName));
        return readMatrix(fileStream, fileName);
    }

    public Matrix setupMatrix(
            FormDataContentDisposition contentDisposition, InputStream excelFileData,
            Integer armyCount, Score defaultScore) {
        LOGGER.info(String.format("Got file <%s> of type <%s> (%d bytes) (stream %s)",
                Objects.toString(contentDisposition.getFileName(), "-"),
                Objects.toString(contentDisposition.getType(), "-"),
                contentDisposition.getSize(),
                excelFileData != null ? "ok" : "null"));
        if (ServiceUtils.hasFileAttached(contentDisposition)) {
            return uploadMatrix(contentDisposition, excelFileData);
        }
        else {
            LOGGER.info(String.format("No file provided: using default %s with %d armies", defaultScore, armyCount));
            return resetMatrix(armyCount, defaultScore);
        }
    }

    public Optional<Army> updateArmyName(Optional<Matrix> matrix, boolean isRow, int index, String givenName) {
        String newName = ServiceUtils.trimName(givenName);
        Optional<Army> newArmy = matrix
                .map(m -> m.getArmies(isRow))
                .filter(list -> list.size() > index)
                .map(list -> list.get(index))
                .map(oldArmy -> Army.renamed(oldArmy, newName));
        newArmy.ifPresent(army -> {
                Matrix m = matrix.get();
                List<Army> armies = m.getArmies(isRow);
                List<Army> collect = IntStream.range(0, armies.size())
                        .mapToObj(i -> i == index ? army : armies.get(i))
                        .collect(Collectors.toList());
                m.setArmies(isRow, collect);
            });
        return newArmy;
    }

    public void writeExcelFile(Matrix matrix, OutputStream os, FileExtensionSupport support) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(os);
        MatrixWriter xlsMatrixWriter = support.getFactory().getWriterBuilder().newWriter(bos);
        xlsMatrixWriter.write(matrix);
        bos.flush();
        // No `bos.close()`
    }
}
