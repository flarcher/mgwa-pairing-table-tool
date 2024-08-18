package org.mgwa.w40k.pairing.api.service;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.mgwa.w40k.pairing.Army;
import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.MatrixReader;
import org.mgwa.w40k.pairing.matrix.MatrixWriter;
import org.mgwa.w40k.pairing.matrix.Score;
import org.mgwa.w40k.pairing.matrix.xls.XlsMatrixReader;
import org.mgwa.w40k.pairing.matrix.xls.XlsMatrixWriter;
import org.mgwa.w40k.pairing.state.AppState;
import org.mgwa.w40k.pairing.util.LoggerSupplier;

import java.io.*;
import java.nio.file.Path;
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

    public MatrixService(AppState state) {
        this.state = state;
    }

    private final AppState state;

    private Optional<Matrix> loadMatrixFile(Path path) {
        // Waiting loading the file
        try (MatrixReader matrixReader = XlsMatrixReader.fromFile(path.toFile())) {
            Matrix matrix = matrixReader.get();
            LOGGER.info(String.format("Using matrix of size %s", matrix.getSize()));
            return Optional.of(matrix);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Impossible to read file", e);
            return Optional.empty();
        }
    }

    private static String trimName(String name) {
        return Optional.ofNullable(name).orElse("").trim();
    }

    public void setTeamNames(String rowsTeamName, String columnsTeamName) {
        String trimedRowsTeamName = trimName(rowsTeamName);
        String trimedColsTeamName = trimName(columnsTeamName);
        Stream.of(trimedRowsTeamName, trimedColsTeamName).forEach(name -> {
            if (name.isBlank() || name.length() > MAX_ARMY_NAME_LENGTH) {
                throw ServiceUtils.badRequest(String.format("Invalid army name (max length is %d)", MAX_ARMY_NAME_LENGTH));
            }
        });
        if (trimedRowsTeamName.equalsIgnoreCase(trimedColsTeamName)) {
            throw ServiceUtils.badRequest("Team names must be different");
        }
        state.setRowTeamName(trimedRowsTeamName);
        state.setColTeamName(trimedColsTeamName);
    }

    private Matrix resetMatrix(Integer armyCount, Score defaultScore) {
        // Input checks
        if (armyCount == null || armyCount < MIN_ARMY_COUNT || armyCount > MAX_ARMY_COUNT) {
            throw ServiceUtils.badRequest(String.format("Invalid army count (must be between %d and %d)", MIN_ARMY_COUNT, MAX_ARMY_COUNT));
        }
        Score internalDefaultScore = Optional.ofNullable(defaultScore).orElse(Score.newDefault());

        // State update
        state.setArmyCount(armyCount);
        Optional<Matrix> currentMatrix = state.getMatrix();
        Matrix matrix;
        /*if (currentMatrix.isPresent()) {
            state.forceArmyCountConsistency(internalDefaultScore);
            matrix = currentMatrix.get();
        }
        else {*/
        matrix = loadMatrixDefault(armyCount, internalDefaultScore);
        state.setMatrix(matrix);
        /*}*/
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

    private static Matrix readMatrix(InputStream excelFileData) {
        try (MatrixReader reader = XlsMatrixReader.fromStream(new BufferedInputStream(excelFileData))) {
            return reader.get();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to parse matrix file", e);
            throw ServiceUtils.badRequest("Invalid matrix file content");
        }
    }

    private Matrix uploadMatrix(FormDataContentDisposition contentDisposition, InputStream excelFileData) {
        String fileName = contentDisposition.getFileName();
        Objects.requireNonNull(fileName);
        Objects.requireNonNull(excelFileData);
        if (fileName.isBlank()) {
            throw ServiceUtils.badRequest("File name is mandatory for upload");
        }
        LOGGER.info(String.format("Reading file %s", fileName));
        Matrix newMatrix = readMatrix(excelFileData);
        state.setMatrix(newMatrix);
        state.setArmyCount(newMatrix.getSize());
        state.setMatrixFilePath(Path.of(fileName)); // Does not provide the folder :/
        return newMatrix;
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

    public void updateScore(int row, int column, Score newScore) {
        int armyCount = state.getArmyCount();
        Stream.of( row, column ).forEach( index -> {
            if (index < 0 || index >= armyCount) {
                throw ServiceUtils.badRequest("Invalid index " + index);
            }
        });
        state.getMatrix().ifPresent(m -> {
            m.setScore(row, column, newScore);
        });
    }

    public void updateTeamName(boolean isRow, String givenName) {
        String newName = trimName(givenName);
        Optional.of(newName).ifPresent(isRow ? state::setRowTeamName : state::setColTeamName);
    }

    public Optional<Army> updateArmyName(boolean isRow, int index, String givenName) {
        String newName = trimName(givenName);
        Optional<Army> newArmy = state.getMatrix()
                .map(m -> m.getArmies(isRow))
                .filter(list -> list.size() > index)
                .map(list -> list.get(index))
                .map(oldArmy -> Army.renamed(oldArmy, newName));
        newArmy.ifPresent(army -> {
                List<Army> armies = state.getMatrix().get().getArmies(isRow);
                List<Army> collect = IntStream.range(0, armies.size())
                        .mapToObj(i -> i == index ? army : armies.get(i))
                        .collect(Collectors.toList());
                state.getMatrix().get().setArmies(isRow, collect);
            });
        return newArmy;
    }

    public void writeExcelFile(OutputStream os) throws IOException {
        if (state.getMatrix().isPresent()) {
            BufferedOutputStream bos = new BufferedOutputStream(os);
            MatrixWriter xlsMatrixWriter = XlsMatrixWriter.fromStream(bos);
            xlsMatrixWriter.write(state.getMatrix().get());
            bos.flush();
            // No `bos.close()`
        }
    }
}
