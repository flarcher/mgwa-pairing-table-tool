package org.mgwa.w40k.pairing.gui;

import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.mgwa.w40k.pairing.LabelGetter;
import org.mgwa.w40k.pairing.util.LoggerSupplier;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * <h1>JavaFX application main class.</h1>
 *
 * <p>This class describes the main logic around the screen and the application.</p>
 *
 * <p>Here is the method calls sequence for the start of the application:</p>
 * <ol>
 *     <li>At first the {@link #launch(Consumer, Runnable, Runnable)} static method is called from the Java main method {@code Main.main(String[] args)}</li>
 *     <li>Then through the {@link #launch(String...)} method of the JavaFX abstract class...</li>
 *     <li>Our method {@link #start(Stage)} gets finally called</li>
 * </ol>
 */
public class AppWindow extends Application implements AutoCloseable {

	//--- Launch

	private static Consumer<AutoCloseable> onInit;
	private static Runnable onClose;
	private static Runnable onNext;

	/**
	 * See the {@code Main} class acts as the main class of the Java application in order to avoid a RuntimeException.
	 * It will eventually call the {@link #start(Stage)} method.
	 */
	public static void launch(
			@Nonnull Consumer<AutoCloseable> injectedOnInit,
			@Nonnull Runnable injectedOnNext,
			@Nonnull Runnable injectedOnClose) {
		onInit  = Objects.requireNonNull(injectedOnInit);
		onClose = Objects.requireNonNull(injectedOnClose);
		onNext  = Objects.requireNonNull(injectedOnNext);

		logger.info("Launching ...");

		//launch(); // Without preloader

		// With a preloader
		AppPreloader.setLabelGetter(labelGetter);
		LauncherImpl.launchApplication(AppWindow.class, AppPreloader.class, new String[0]); // With a preloader
	}

	private static final Logger logger = LoggerSupplier.INSTANCE.getLogger();
	private static final LabelGetter labelGetter = LabelGetter.create();

	private static final AtomicBoolean isClosed = new AtomicBoolean(false);

	//--- Mechanics

	@Override
	public void init() throws Exception {
		logger.info("Initializing ...");
		onInit.accept(this); // The important line is here
		logger.info("Initialized");
	}

	private void closeAtomically() {
		if (isClosed.compareAndSet(false, true)) {
			logger.info("Stopping window");
			Platform.exit();
			onClose.run(); // The important line is here
		}
	}

	@Override
	public void stop() throws Exception {
		closeAtomically();
	}

	@Override
	public void close() throws Exception {
		closeAtomically();
	}

	/**
	 * Switch the scene displayed.
	 * @param supplier The new scene.
	 */
	private void goToScene(Stage stage, Supplier<Scene> supplier) {
		Objects.requireNonNull(stage);
		if (stage.isShowing()) {
			stage.close();
		}
		stage.setScene(supplier.get());
		stage.show();
	}

	//--- Scenes

    @Override
    public void start(Stage stage) {
		logger.info("Starting ...");
		// Opens the web-app
		onNext.run();
		// Display new window
		goToScene(stage, new MessageScene(labelGetter.getLabel("see.webapp")));
		// Configure the exit button
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				try {
					AppWindow.this.stop(); // It will call the "onClose" callback
				} catch (Exception exception) {
					throw new IllegalStateException("Internal error", exception);
				}
			}
		});
		logger.info("Started");
    }

}