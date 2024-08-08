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

public class MatrixService {

    public MatrixService(AppState state) {
        this.state = state;
    }

    private final AppState state;

    private static final Logger LOGGER = LoggerSupplier.INSTANCE.getLogger();

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

    private Matrix loadMatrixDefault() {
        List<String> names = IntStream.range(0, state.getArmyCount())
                .mapToObj(Integer::toString)
                .collect(Collectors.toList());
        Matrix matrix = Matrix.createWithoutScores(
                Army.createArmies(names, true),
                Army.createArmies(names, false));
        return matrix.setDefaultScore(Score.newDefault());
    }

    private void resizeMatrix() {
        state.forceArmyCountConsistency(Score.newDefault());
    }
}
