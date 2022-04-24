package org.mgwa.w40k.pairing.gui.scene;

import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.mgwa.w40k.pairing.gui.AppState;
import org.mgwa.w40k.pairing.gui.NodeFactory;

public class WaitingScene implements SceneDefinition {

    public WaitingScene(String initialMessage) {
        this.text = NodeFactory.createText(initialMessage);
    }

    private final Text text;

    public void setMessage(String message) {
        this.text.setText(message);
    }

    @Override
    public Scene getScene(AppState state, Stage stage) {
        GridPane pane = NodeFactory.createGrid(1);
        pane.addRow(0, this.text);
        return new Scene(pane);
    }

}
