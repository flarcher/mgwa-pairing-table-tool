package org.mgwa.w40k.pairing.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import org.mgwa.w40k.pairing.LabelGetter;
import org.mgwa.w40k.pairing.gui.scene.*;
import org.mgwa.w40k.pairing.util.LoggerSupplier;
import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.MatrixReader;
import org.mgwa.w40k.pairing.matrix.xls.XlsMatrixReader;

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
 *     <li>At first the {@link #main()} static method is called from the Java main method {@link Main#main(String[])}</li>
 *     <li>Then through the {@link #launch(String...)} method of the JavaFX abstract class...</li>
 *     <li>Our method {@link #start(Stage)} gets finally called</li>
 * </ol>
 */
public class AppWindow extends Application {

	private static final AppState state = new AppState();

	public static AppState getState() {
		return state;
	}

	/**
	 * See the {@link Main} class acts as the main class of the Java application in order to avoid a RuntimeException.
	 * It will eventually call the {@link #start(Stage)} method.
	 */
	public static void main() {
		launch();
	}

	private final Logger logger = LoggerSupplier.INSTANCE.getLogger();
	private final LabelGetter labelGetter = LabelGetter.create();

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

	//--- Scenes

	private TeamDefinitionScene teamDefinition;
	private MatrixSetupScene matrixSetup;

    @Override
    public void start(Stage stage) {
		this.stage = stage;
		teamDefinition = new TeamDefinitionScene(labelGetter, this::toMatrixDisplay);
		goToScene(teamDefinition);
    }

	private void displayError(String msg, SceneDefinition sceneTarget) {
		goToScene(new InfoScene(
			() -> goToScene(sceneTarget),
			msg, labelGetter.getLabel("ok")));
	}

	private static final int DEFAULT_ARMY_COUNT = 3;

	private Matrix loadMatrixFile(Path path) {
		// Waiting loading the file
		try (MatrixReader matrixReader = XlsMatrixReader.fromFile(path.toFile())) {
			Matrix matrix = matrixReader.get();
			logger.info(String.format("Using matrix of size %s", matrix.getSize()));
			return matrix;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Impossible to read file", e);
			displayError(String.format("%s %s", labelGetter.getLabel("can-not-read-file"), path), teamDefinition);
			return null;
		}
	}

	private void toMatrixDisplay() {

		if (!state.getMatrix().isPresent()) {
			Matrix matrix;
			if (state.getMatrixFilePath().isPresent()) {
				Path path = state.getMatrixFilePath().get();
				// Waiting loading the file
				goToScene(new WaitingScene(String.format("Loading file %s", path)));
				matrix = loadMatrixFile(path);
			} else {
				logger.info("Using empty matrix");
				matrix = new Matrix(DEFAULT_ARMY_COUNT);
			}
			state.setMatrix(matrix);
		}

		// Ending the loading
		if (matrixSetup == null) {
			matrixSetup = new MatrixSetupScene(labelGetter);
		}
		goToScene(matrixSetup);
	}
}