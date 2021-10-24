package org.mgwa.w40k.pairing.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import org.mgwa.w40k.pairing.util.LoggerSupplier;
import org.mgwa.w40k.pairing.gui.scene.InfoScene;
import org.mgwa.w40k.pairing.gui.scene.MatrixSetupScene;
import org.mgwa.w40k.pairing.gui.scene.SceneDefinition;
import org.mgwa.w40k.pairing.gui.scene.TeamDefinitionScene;
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

	/**
	 * See the {@link Main} class acts as the main class of the Java application in order to avoid a RuntimeException.
	 */
	public static void main() {
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

	//--- Scenes

	private TeamDefinitionScene teamDefinition;
	private MatrixSetupScene matrixSetup;

    @Override
    public void start(Stage stage) {
		this.stage = stage;
		teamDefinition = new TeamDefinitionScene(this::toMatrixDisplay);
		goToScene(teamDefinition);
    }

	private void displayError(String msg, SceneDefinition sceneTarget) {
		goToScene(new InfoScene(
			() -> goToScene(sceneTarget),
			msg));
	}

	private static final int DEFAULT_ARMY_COUNT = 3;

	private void toMatrixDisplay() {

		if (!state.getMatrix().isPresent()) {
			Matrix matrix;
			if (state.getMatrixFilePath().isPresent()) {
				Path path = state.getMatrixFilePath().get();
				try (MatrixReader matrixReader = XlsMatrixReader.fromFile(path.toFile())) {
					matrix = matrixReader.get();
					logger.info(String.format("Using matrix of size %s", matrix.getSize()));
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Impossible to read file", e);
					displayError(String.format("Unable to read file %s", path), teamDefinition);
					return;
				}
			} else {
				logger.info("Using empty matrix");
				matrix = new Matrix(DEFAULT_ARMY_COUNT);
			}
			state.setMatrix(matrix);
		}

		if (matrixSetup == null) {
			matrixSetup = new MatrixSetupScene();
		}
		goToScene(matrixSetup);
	}
}