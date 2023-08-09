package org.mgwa.w40k.pairing.gui.scene;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.mgwa.w40k.pairing.Army;
import org.mgwa.w40k.pairing.state.AppState;
import org.mgwa.w40k.pairing.gui.NodeFactory;
import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.Score;
import org.mgwa.w40k.pairing.util.LoggerSupplier;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Scene that helps at defining scores in the matrix.
 */
public class MatrixSetupScene extends AbstractMainScene {

	public MatrixSetupScene(Function<String, String> labelGetter, Runnable previous, Runnable next) {
		super(labelGetter, 3);
		this.previousAction = Objects.requireNonNull(previous);
		this.nextAction = Objects.requireNonNull(next);
	}

	private final Runnable previousAction;
	private final Runnable nextAction;

	private static List<TextField> textFieldOfNames(List<Army> armies) {
		int nodeWidth = SCENE_WIDTH / (armies.size() + 1);
		return armies.stream()
			.map(army -> {
				TextField text = new TextField();
				text.setText(army.getName());
				text.maxWidth(nodeWidth);
				GridPane.setHalignment(text, HPos.CENTER);
				return text;
			})
			.collect(Collectors.toList());
	}

	private static List<Army> withNewNames(List<Army> armies, List<TextField> newNames) {
		if (armies.size() != newNames.size()) {
			throw new IllegalStateException();
		}
		return IntStream.range(0, armies.size())
			.mapToObj(index -> {
				Army originalArmy = armies.get(index);
				TextField newName = newNames.get(index);
				return Army.renamed(originalArmy, newName.getText());
			})
			.collect(Collectors.toList());
	}

	private static void updateState(Matrix matrix, List<TextField> rowArmyNames, List<TextField> colArmyNames) {
		matrix.setArmies(true, withNewNames(matrix.getArmies(true), rowArmyNames));
		matrix.setArmies(false, withNewNames(matrix.getArmies(false), colArmyNames));
	}

	@Override
	protected void buildScene(AppState state, Stage stage) {

		Logger logger = LoggerSupplier.INSTANCE.getLogger();
		Matrix matrix = state.getMatrix().orElseThrow();
		List<Army> rowArmies = matrix.getArmies(true);
		List<Army> colArmies = matrix.getArmies(false);
		List<TextField> rowArmyNames = textFieldOfNames(rowArmies);
		List<TextField> colArmyNames = textFieldOfNames(colArmies);
		GridPane grid = NodeFactory.createGrid(matrix.getSize() + 1);
		grid.setGridLinesVisible(true);
		int nodeWidth = SCENE_WIDTH / (matrix.getSize() + 1);
		IntStream.range(0, matrix.getSize())
			.forEach(i -> {

				grid.add(colArmyNames.get(i), i + 1, 0, 1, 1);
				grid.add(rowArmyNames.get(i), 0, i + 1, 1, 1);

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

		addNode(grid, getRowIndex(), 0, 1, 3, HPos.CENTER);

		newRow();
		Button prevButton = NodeFactory.createButton(
			labelGetter.apply("previous"),
			e -> {
				updateState(matrix, rowArmyNames, colArmyNames);
				previousAction.run();
			});
		addNode(prevButton, getRowIndex(), 0, 1, 1, HPos.LEFT);
		Button nextButton = NodeFactory.createButton(
			labelGetter.apply("next"),
			e -> {
				updateState(matrix, rowArmyNames, colArmyNames);
				nextAction.run();
			});
		addNode(nextButton, getRowIndex(), 2, 1, 1, HPos.RIGHT);
	}

}
