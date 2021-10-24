package org.mgwa.w40k.pairing.gui.scene;

import javafx.geometry.HPos;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.mgwa.w40k.pairing.gui.AppState;
import org.mgwa.w40k.pairing.gui.NodeFactory;
import org.mgwa.w40k.pairing.matrix.Matrix;

import java.util.Objects;

/**
 * Scene that helps at defining scores in the matrix.
 */
public class MatrixSetupScene extends AbstractMainScene {

	public MatrixSetupScene(Matrix matrix) {
		super(matrix.getSize());
		this.matrix = Objects.requireNonNull(matrix);
	}

	private final Matrix matrix;

	@Override
	protected void buildScene(AppState state, Stage stage) {
		Text txt = NodeFactory.createText("hello");
		addNode(txt, getRowIndex(), 0, 1, matrix.getSize(), HPos.LEFT);
		// TODO
	}

	public Matrix getMatrix() {
		return  matrix;
	}
}
