package org.mgwa.w40k.pairing.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import org.mgwa.w40k.pairing.LoggerSupplier;
import org.mgwa.w40k.pairing.gui.scene.InfoScene;
import org.mgwa.w40k.pairing.gui.scene.SceneDefinition;
import org.mgwa.w40k.pairing.gui.scene.TeamDefinitionScene;

import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <h1>JavaFX application main class.</h1>
 *
 * <p>This class describes the main logic around the screen and the application.</p>
 *
 * <p>Here is the method calls sequence for the start of the application:</p>
 * <ol>
 *     <li>At first the {@link #main(String[])} static method is called from the Java main method {@link Main#main(String[])}</li>
 *     <li>Then through the {@link #launch(String...)} method of the JavaFX abstract class...</li>
 *     <li>Our method {@link #start(Stage)} gets finally called</li>
 * </ol>
 */
public class AppWindow extends Application {

	/**
	 * See the {@link Main} class acts as the main class of the Java application in order to avoid a RuntimeException.
	 */
	public static void main(String[] args) {
		launch();
	}

	private final AppState state = new AppState();
	private final Logger logger = LoggerSupplier.INSTANCE.getLogger();

	private Stage stage;

	/**
	 * Switch the scene displayed.
	 * @param newScene The new scene.
	 */
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