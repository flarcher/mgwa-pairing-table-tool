package org.mgwa.w40k.pairing.api.service;

import org.mgwa.w40k.pairing.Army;
import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.MatrixReader;
import org.mgwa.w40k.pairing.matrix.Score;
import org.mgwa.w40k.pairing.matrix.xls.XlsMatrixReader;
import org.mgwa.w40k.pairing.state.AppState;
import org.mgwa.w40k.pairing.util.LoggerSupplier;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Service about score matrix update.
 */
public class MatrixUpdateService {

    private static final Logger LOGGER = LoggerSupplier.INSTANCE.getLogger();
    private static final int MIN_ARMY_COUNT = 0;
    private static final int MAX_ARMY_COUNT = 10;
    private static final int MAX_ARMY_NAME_LENGTH = 25;

    public MatrixUpdateService(AppState state) {
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

    public Matrix resetMatrix(String rowsTeamName, String columnsTeamName, Long armyCount) {
        if (armyCount == null || armyCount < MIN_ARMY_COUNT || armyCount > MAX_ARMY_COUNT) {
            throw ServiceUtils.badRequest(String.format("Invalid army count (must be between %d and %d)", MIN_ARMY_COUNT, MAX_ARMY_COUNT));
        }
        Stream.of(rowsTeamName, columnsTeamName).forEach(name -> {
            if (name == null || name.isBlank() || name.length() > MAX_ARMY_NAME_LENGTH) {
                throw ServiceUtils.badRequest(String.format("Invalid army name (max length is %d)", MAX_ARMY_NAME_LENGTH));
            }
        });
        state.setRowTeamName(rowsTeamName);
        state.setColTeamName(columnsTeamName);
        state.setArmyCount(armyCount.intValue());
        Optional<Matrix> currentMatrix = state.getMatrix();
        Matrix matrix;
        if (currentMatrix.isPresent()) {
            state.forceArmyCountConsistency(Score.newDefault());
            matrix = currentMatrix.get();
        }
        else {
            matrix = loadMatrixDefault(armyCount.intValue());
            state.setMatrix(matrix);
        }
        return matrix;
    }

    private static Matrix loadMatrixDefault(int armyCount) {
        List<String> names = IntStream.range(0, armyCount)
                .mapToObj(Integer::toString)
                .collect(Collectors.toList());
        Matrix matrix = Matrix.createWithoutScores(
                Army.createArmies(names, true),
                Army.createArmies(names, false));
        return matrix.setDefaultScore(Score.newDefault());
    }

}
