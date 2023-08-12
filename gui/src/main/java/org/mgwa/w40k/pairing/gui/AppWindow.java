package org.mgwa.w40k.pairing.gui;

import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.mgwa.w40k.pairing.Army;
import org.mgwa.w40k.pairing.LabelGetter;
import org.mgwa.w40k.pairing.gui.scene.*;
import org.mgwa.w40k.pairing.matrix.Score;
import org.mgwa.w40k.pairing.state.AppState;
import org.mgwa.w40k.pairing.util.LoggerSupplier;
import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.MatrixReader;
import org.mgwa.w40k.pairing.matrix.xls.XlsMatrixReader;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <h1>JavaFX application main class.</h1>
 *
 * <p>This class describes the main logic around the screen and the application.</p>
 *
 * <p>Here is the method calls sequence for the start of the application:</p>
 * <ol>
 *     <li>At first the {@link #launch(AppState, Runnable, Runnable)} static method is called from the Java main method {@code Main.main(String[] args)}</li>
 *     <li>Then through the {@link #launch(String...)} method of the JavaFX abstract class...</li>
 *     <li>Our method {@link #start(Stage)} gets finally called</li>
 * </ol>
 */
public class AppWindow extends Application {

	//--- Launch

	private static AppState state;
	private static Runnable onInit;
	private static Runnable onClose;

	/**
	 * See the {@code Main} class acts as the main class of the Java application in order to avoid a RuntimeException.
	 * It will eventually call the {@link #start(Stage)} method.
	 */
	public static void launch(
			@Nonnull AppState injectedState,
			@Nonnull Runnable injectedOnInit,
			@Nonnull Runnable injectedOnClose) {
		state = injectedState;
		onInit = injectedOnInit;
		onClose = injectedOnClose;

		logger.info("Launching ...");

		//launch(); // Without preloader

		// With a preloader
		AppPreloader.setLabelGetter(labelGetter);
		LauncherImpl.launchApplication(AppWindow.class, AppPreloader.class, new String[0]); // With a preloader
	}

	private static final Logger logger = LoggerSupplier.INSTANCE.getLogger();
	private static final LabelGetter labelGetter = LabelGetter.create();

	//--- Mechanics

	@Override
	public void init() throws Exception {
		logger.info("Initializing ...");
		onInit.run(); // The important line is here
		logger.info("Initialized");
	}

	@Override
	public void stop() throws Exception {
		logger.info("Stopping ...");
		onClose.run(); // The important line is here
		logger.info("Stopped");
	}

	private Stage stage;

	/**
	 * Switch the scene displayed.
	 * @param newScene The new scene.
	 */
	private void goToScene(SceneDefinition newScene) {
		Objects.requireNonNull(stage);
		if (stage.isShowing()) {
			stage.close();
		}
		stage.setScene(newScene.getScene(state, stage));
		stage.show();
	}

	//--- Scenes

	private TeamDefinitionScene teamDefinition;
	private MatrixSetupScene matrixSetup;

    @Override
    public void start(Stage stage) {
		logger.info("Starting ...");
		this.stage = stage;
		teamDefinition = new TeamDefinitionScene(labelGetter, this::toMatrixDisplay);
		goToScene(teamDefinition);
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				try {
					AppWindow.this.stop(); // It will call the "onClose" callback
				} catch (Exception exception) {
					throw new IllegalStateException("Internal error", exception);
				}
				Platform.exit();
				System.exit(0);
			}
		});
		logger.info("Started");
    }

	private void backToTeamDefinition() {
		goToScene(teamDefinition);
	}

	private void displayError(String msg, SceneDefinition sceneTarget) {
		goToScene(new InfoScene(
			() -> goToScene(sceneTarget),
			msg, labelGetter.getLabel("ok")));
	}

	private Matrix loadMatrixDefault() {
		List<String> names = IntStream.range(0, state.getArmyCount())
				.mapToObj(Integer::toString)
				.collect(Collectors.toList());
		Matrix matrix = Matrix.createWithoutScores(
				Army.createArmies(names, true),
				Army.createArmies(names, false));
		return matrix.setDefaultScore(Score.newDefault());
	}

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
				matrix = loadMatrixDefault();
			}
			state.setMatrix(matrix);
		}
		else {
			Matrix matrix = state.getMatrix().get();
			logger.info(String.format("Resizing matrix from %d to %d", matrix.getSize(), state.getArmyCount()));
			state.forceArmyCountConsistency(Score.newDefault());
		}

		// Ending the loading
		if (matrixSetup == null) {
			matrixSetup = new MatrixSetupScene(labelGetter, this::backToTeamDefinition, this::nextAssignment);
		}
		goToScene(matrixSetup);
	}

	private void nextAssignment() {
		logger.info("Ready to Assign!");
		// TODO
	}
}