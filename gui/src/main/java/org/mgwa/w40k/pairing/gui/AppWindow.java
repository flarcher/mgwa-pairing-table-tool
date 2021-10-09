package org.mgwa.w40k.pairing.gui;

import org.mgwa.w40k.pairing.gui.scene.InfoScene;
import org.mgwa.w40k.pairing.gui.scene.SceneDefinition;
import org.mgwa.w40k.pairing.gui.scene.TeamDefinitionScene;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import java.util.Objects;

public class AppWindow extends Application {

	/**
	 * Needed in order to avoid a RuntimeException
	 */
	public static void main(String[] args) {
		launch();
	}

	private final AppState state = new AppState();

	private Stage stage;

	private void goToScene(SceneDefinition newScene) {
		Objects.requireNonNull(stage);
		stage.setScene(newScene.getScene(state, stage));
		stage.show();
	}

	private SceneDefinition getInfoScene() {
		return new InfoScene(
				Platform::exit,
				String.format("Team names are %s and %s\n\nYou have%s the table choice.\n\nGiven file is: %s",
						state.getRowTeamName(),
						state.getColTeamName(),
						state.youHaveTheTableToken() ? "" : " not",
						state.getMatrixFilePath().map(Objects::toString).orElse("<none>"))
			);
	}

    @Override
    public void start(Stage stage) {
		this.stage = stage;
		goToScene(new TeamDefinitionScene(() -> goToScene(getInfoScene())));
    }

}