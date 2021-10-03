package flarcher.pairing.gui.scene;

import flarcher.pairing.gui.AppState;
import javafx.scene.Scene;

/**
 * Defines a GUI scene/screen.
 */
public interface SceneDefinition {

	Scene getScene(AppState state);

}
