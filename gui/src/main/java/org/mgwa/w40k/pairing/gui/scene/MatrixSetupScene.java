package org.mgwa.w40k.pairing.gui.scene;

import javafx.geometry.HPos;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.mgwa.w40k.pairing.Army;
import org.mgwa.w40k.pairing.gui.AppState;
import org.mgwa.w40k.pairing.gui.NodeFactory;
import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.Score;
import org.mgwa.w40k.pairing.util.LoggerSupplier;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * Scene that helps at defining scores in the matrix.
 */
public class MatrixSetupScene extends AbstractMainScene {

	public MatrixSetupScene() {
		super(1);
	}

	@Override
	protected void buildScene(AppState state, Stage stage) {

		Logger logger = LoggerSupplier.INSTANCE.getLogger();
		Matrix matrix = state.getMatrix().orElseThrow();
		List<Army> rowArmies = matrix.getArmies(true);
		List<Army> colArmies = matrix.getArmies(false);
		GridPane grid = NodeFactory.createGrid(matrix.getSize() + 1);
		IntStream.range(0, matrix.getSize())
			.forEach(i -> {

				String colArmyName = colArmies.get(i).getName();
				Text text = NodeFactory.createText(colArmyName);
				GridPane.setHalignment(text, HPos.CENTER);
				grid.add(text, i + 1, 0, 1, 1);

				String rowArmyName = rowArmies.get(i).getName();
				text = NodeFactory.createText(rowArmyName);
				GridPane.setHalignment(text, HPos.CENTER);
				grid.add(text, 0, i + 1, 1, 1);

				for (int j = matrix.getSize() - 1; j >= 0; j--) {
					final int rowIndex = i;
					final int columnIndex = j;
					Score score;
					Optional<Score> scoreOpt = matrix.getScore(i, j);
					if (!scoreOpt.isPresent()) {
						logger.warning(String.format("No score at %s:%d", rowIndex, columnIndex));
						score = new Score();
						matrix.setScore(i, j, score);
					}
					else {
						score = scoreOpt.get();
						logger.finer(String.format("Using score %d at %d:%d", score.getValue(), rowIndex, columnIndex));
					}
					int scoreValue = scoreOpt
							.map(Score::getValue)
							.orElse(10);
					TextField scoreField = NodeFactory.createScoreField(scoreValue);
					GridPane.setHalignment(scoreField, HPos.CENTER);
					grid.add(scoreField, i + 1, j + 1, 1, 1);
					scoreField.textProperty().addListener((obs, oldValue, newValue) -> {
						logger.finer(String.format("new score : from %d to %d at %d:%d", oldValue, newValue, rowIndex, columnIndex));
						score.setValue(Integer.parseUnsignedInt(newValue));
					});
				}
			});

		addRow(HPos.CENTER, grid);
	}

}
