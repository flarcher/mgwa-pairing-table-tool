package org.mgwa.w40k.pairing.gui;

import org.mgwa.w40k.pairing.gui.scene.InfoScene;
import org.mgwa.w40k.pairing.gui.scene.SceneDefinition;
import org.mgwa.w40k.pairing.gui.scene.TeamDefinitionScene;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Objects;

public class AppWindow extends Application {

	/**
	 * Needed in order to avoid a RuntimeException
	 */
	public static void main(String[] args) {
		launch();
	}

	private final AppState state = AppState.INSTANCE;

	private Stage stage;

	private void goToScene(SceneDefinition newScene) {
		Objects.requireNonNull(stage);
		stage.setScene(newScene.getScene(state));
		stage.show();
	}

	private SceneDefinition getInfoScene() {
		return new InfoScene(
				Platform::exit,
				String.format("Team names are %s and %s\nYou have%s the table choice.",
						state.getRowTeamName(),
						state.getColTeamName(),
						state.youHaveTheTableToken() ? "" : " not")
			);
	}

	private File selectFile(String label) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(label);
		fileChooser.showOpenDialog(stage);
		// TODO
		return null;
	}

    @Override
    public void start(Stage stage) {
		this.stage = stage;
		Scene teamNamesScene = new TeamDefinitionScene(() -> {
			goToScene(getInfoScene());
		}).getScene(state);
		stage.setScene(teamNamesScene);
		stage.show();
    }

}