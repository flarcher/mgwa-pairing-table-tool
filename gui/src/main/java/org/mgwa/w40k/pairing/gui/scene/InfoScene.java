package org.mgwa.w40k.pairing.gui.scene;

import javafx.stage.Stage;
import org.mgwa.w40k.pairing.state.AppState;
import org.mgwa.w40k.pairing.gui.NodeFactory;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;

public class InfoScene implements SceneDefinition {

	public InfoScene(Runnable next, String message, String buttonLabel) {
		this.next = next;
		this.message = message;
		this.buttonLabel = buttonLabel;
	}

	private final Runnable next;
	private final String message;
	private final String buttonLabel;

	@Override
	public Scene getScene(AppState state, Stage stage) {
		GridPane pane = NodeFactory.createGrid(1);
		pane.addRow(0, NodeFactory.createText(message));
		pane.addRow(1, NodeFactory.createButton("OK", e -> next.run()));
		return new Scene(pane/*, 320, 200*/);
	}
}
