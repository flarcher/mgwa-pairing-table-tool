package org.mgwa.w40k.pairing.gui.scene;

import javafx.stage.Stage;
import org.mgwa.w40k.pairing.gui.AppState;
import org.mgwa.w40k.pairing.gui.NodeFactory;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.util.Objects;
import java.util.function.Function;

/**
 * Common frame for a scene.
 */
abstract class AbstractMainScene implements SceneDefinition {

	protected static final int SCENE_WIDTH = 800;
	protected static final int SCENE_HEIGHT = 600;

	public AbstractMainScene(Function<String, String> labelGetter, int columnCount) {
		this.columnCount = columnCount;
		this.labelGetter = labelGetter;
	}

	private GridPane grid; // Only for convenience (will be re-created with each display)
	private int rowIndex = 0;
	private final int columnCount;
	protected final Function<String, String> labelGetter;

	protected final void addRow(HPos pos, Node... nodes) {
		assert nodes.length <= columnCount;
		int colIndex = 0;
		for (Node node : nodes) {
			addNode(node, rowIndex, colIndex++, 1, 1, pos);
		}
		rowIndex++;
	}

	protected final void addNode(Node node, int rowNumber, int colNumber, int rowSpan, int colSpan, HPos pos) {
		Objects.requireNonNull(grid);
		GridPane.setFillWidth(node, true);
		GridPane.setHalignment(node, pos);
		grid.add(node, colNumber, rowNumber, colSpan, rowSpan);
	}

	protected final int getRowIndex() { return rowIndex; }
	protected final int newRow() { return  ++rowIndex; }

	protected abstract void buildScene(AppState state, Stage stage);

	@Override
	public final Scene getScene(AppState state, Stage stage) {
		grid = NodeFactory.createGrid(columnCount);
		Text headerLabel = NodeFactory.createHeaderLabel(labelGetter.apply("header"));
		addNode(headerLabel, getRowIndex(), 0, 1, columnCount, HPos.LEFT);
		newRow();

		buildScene(state, stage);
		newRow();

		Text footerLabel = NodeFactory.createFooterLabel(labelGetter.apply("footer"));
		addNode(footerLabel, getRowIndex(), 0, 1, columnCount, HPos.RIGHT);
		newRow();

		return new Scene(grid, SCENE_WIDTH, SCENE_HEIGHT);
	}
}
