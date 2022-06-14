package org.mgwa.w40k.pairing.gui.scene;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * Scene that helps at defining scores in the matrix.
 */
public class MatrixSetupScene extends AbstractMainScene {

	public MatrixSetupScene(Function<String, String> labelGetter) {
		super(labelGetter, 1);
	}

	@Override
	protected void buildScene(AppState state, Stage stage) {

		Logger logger = LoggerSupplier.INSTANCE.getLogger();
		Matrix matrix = state.getMatrix().orElseThrow();
		List<Army> rowArmies = matrix.getArmies(true);
		List<Army> colArmies = matrix.getArmies(false);
		GridPane grid = NodeFactory.createGrid(matrix.getSize() + 1);
		int nodeWidth = SCENE_WIDTH / (matrix.getSize() + 1);
		IntStream.range(0, matrix.getSize())
			.forEach(i -> {

				String colArmyName = colArmies.get(i).getName();
				TextField text = new TextField();
				text.setText(colArmyName);
				text.maxWidth(nodeWidth);
				GridPane.setHalignment(text, HPos.CENTER);
				grid.add(text, i + 1, 0, 1, 1);

				String rowArmyName = rowArmies.get(i).getName();
				text = new TextField();
				text.setText(rowArmyName);
				text.maxWidth(nodeWidth);
				GridPane.setHalignment(text, HPos.CENTER);
				grid.add(text, 0, i + 1, 1, 1);

				for (int j = matrix.getSize() - 1; j >= 0; j--) {
					final int rowIndex = i;
					final int columnIndex = j;
					Score score;
					Optional<Score> scoreOpt = matrix.getScore(i, j);
					if (!scoreOpt.isPresent()) {
						logger.warning(String.format("No score at %s:%d", rowIndex, columnIndex));
						score = Score.newDefault();
						matrix.setScore(i, j, score);
					}
					else {
						score = scoreOpt.get();
						logger.finer(String.format("Using score %s at %d:%d", score, rowIndex, columnIndex));
					}

					TextField minScoreField = NodeFactory.createScoreField(score.getMinValue());
					TextField maxScoreField = NodeFactory.createScoreField(score.getMaxValue());
					Text scoreSeparator = new Text(" - ");
					HBox hbox = new HBox(minScoreField, scoreSeparator, maxScoreField);
					hbox.setPrefWidth(nodeWidth);
					hbox.setMaxWidth(nodeWidth);
					hbox.setAlignment(Pos.CENTER);
					GridPane.setHalignment(hbox, HPos.CENTER);
					grid.add(hbox, j + 1, i + 1, 1, 1); // Column, Row, span, span
					minScoreField.textProperty().addListener((obs, oldValue, newValue) -> {
						logger.finer(String.format("new minimum score : from %s to %s at %d:%d", oldValue, newValue, rowIndex, columnIndex));
						score.updateMinValue(Integer.parseUnsignedInt(newValue));
					});
					maxScoreField.textProperty().addListener((obs, oldValue, newValue) -> {
						logger.finer(String.format("new maximum score : from %s to %s at %d:%d", oldValue, newValue, rowIndex, columnIndex));
						score.updateMaxValue(Integer.parseUnsignedInt(newValue));
					});
				}
			});

		addRow(HPos.CENTER, grid);

		// TODO: Add a previous button
		// TODO: Add a next button (update the army names !)
	}

}
