package org.mgwa.w40k.pairing.gui.scene;

import javafx.stage.Stage;
import org.mgwa.w40k.pairing.state.AppState;
import javafx.scene.Scene;

/**
 * Defines a GUI scene/screen.
 */
public interface SceneDefinition {

	Scene getScene(AppState state, Stage stage);

}
