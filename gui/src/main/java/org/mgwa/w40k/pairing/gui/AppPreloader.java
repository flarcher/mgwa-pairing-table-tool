package org.mgwa.w40k.pairing.gui;

import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.mgwa.w40k.pairing.state.AppState;

import java.util.Objects;
import java.util.function.Function;

/**
 * Splash screen created from {@link AppWindow#launch(AppState, Runnable, Runnable)}.
 */
public class AppPreloader extends Preloader {

    private static final double WIDTH = 400;
    private static final double HEIGHT = 400;

    private Stage preloaderStage;
    private Scene scene;

    private static Function<String, String> labelGetter;

    static void setLabelGetter(Function<String, String> labelGetter) {
        AppPreloader.labelGetter = labelGetter;
    }

    /**
     * Warning: A zero-argument constructor is mandatory
     */
    public AppPreloader() {}

    @Override
    public void init() throws Exception {

        // If preloader has complex UI it's initialization can be done in MyPreloader#init
        Platform.runLater(() -> {
            Objects.requireNonNull(labelGetter);
            Label header = new Label(labelGetter.apply("header"));
            header.setTextAlignment(TextAlignment.LEFT);
            Label progress = new Label(labelGetter.apply("loading"));
            progress.setTextAlignment(TextAlignment.CENTER);
            Label footer = new Label(labelGetter.apply("footer"));
            footer.setTextAlignment(TextAlignment.RIGHT);
            VBox root = new VBox(header, progress, footer);
            root.setAlignment(Pos.CENTER);
            scene = new Scene(root, WIDTH, HEIGHT);
        });
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.preloaderStage = primaryStage;

        // Set preloader scene and show stage.
        preloaderStage.setScene(scene);
        preloaderStage.show();
    }

    @Override
    public void handleApplicationNotification(PreloaderNotification info) {
        // Handle application notification in this point
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification info) {
        if (info.getType() == StateChangeNotification.Type.BEFORE_START) {
            preloaderStage.hide();
        }
    }
}
