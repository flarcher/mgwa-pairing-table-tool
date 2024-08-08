package org.mgwa.w40k.pairing.gui;

import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.util.function.Supplier;

/**
 * A simple scene that displays a text.
 */
class MessageScene implements Supplier<Scene> {

    MessageScene(String initialMessage) {
        this.text = NodeFactory.createText(initialMessage);
    }

    private final Text text;

    public void setMessage(String message) {
        this.text.setText(message);
    }

    @Override
    public Scene get() {
        GridPane pane = NodeFactory.createGrid(1);
        pane.addRow(0, this.text);
        return new Scene(pane);
    }

}
