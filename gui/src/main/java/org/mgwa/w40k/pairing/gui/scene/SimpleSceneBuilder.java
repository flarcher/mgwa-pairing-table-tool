package org.mgwa.w40k.pairing.gui.scene;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.mgwa.w40k.pairing.state.AppState;

public interface SimpleSceneBuilder extends SceneDefinition {

    Scene getScene();

    @Override
    default Scene getScene(AppState state, Stage stage) {
        return getScene();
    }
}
