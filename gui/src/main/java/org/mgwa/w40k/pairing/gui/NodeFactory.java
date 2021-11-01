package org.mgwa.w40k.pairing.gui;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.converter.IntegerStringConverter;
import org.mgwa.w40k.pairing.matrix.Score;

import java.io.InputStream;
import java.util.stream.IntStream;

/**
 * Utility class that creates JavaFX nodes.
 */
public final class NodeFactory {

	private static final Font FONT_REGULAR;
	static {
		InputStream is = NodeFactory.class.getResourceAsStream("/font.ttf");
		if (is == null) {
			throw new IllegalStateException("Font not found");
		}
		FONT_REGULAR = Font.loadFont(is, 20);
	}

	private static final Font FONT_FOOTER = Font.font(FONT_REGULAR.getName(),12);
	private static final Font FONT_HEADER = Font.font(FONT_REGULAR.getName(),42);

	private NodeFactory() {}

	public static Button createButton(String label, EventHandler<Event> handler) {
		Button b = new Button(label);
		b.setFont(FONT_REGULAR);
		//b.setPrefSize(100, 20);
		b.setAlignment(Pos.CENTER);
		b.setOnAction(handler::handle);
		b.setOnMouseClicked(handler);
		return b;
	}

	public static Label createLabel(String labelText, Node button) {
		Label label = new Label();
		label.setText(labelText);
		label.setFont(FONT_REGULAR);
		label.setAlignment(Pos.CENTER_LEFT);
		label.setLabelFor(button);
		return label;
	}

	public static Text createHeaderLabel() {
		Text label = new Text();
		label.setFont(FONT_HEADER);
		label.setText("A W40k E.T.C. Table-Pairing-Tool");
		return label;
	}

	public static Text createFooterLabel() {
		Text label = new Text();
		label.setFont(FONT_FOOTER);
		label.setText("© MGWA non-profit organization");
		return label;
	}

	public static Text createText(String content) {
		Text label = new Text();
		label.setFont(FONT_REGULAR);
		label.setText(content);
		return label;
	}

	public static GridPane createGrid(int columnCount) {
		GridPane grid = new GridPane();
		grid.setPadding(new Insets(10, 10, 10, 10));
		grid.setVgap(5);
		grid.setHgap(5);
		grid.setAlignment(Pos.TOP_CENTER);
		double percentWidth = 100.0 / columnCount;
		IntStream.range(0, columnCount)
				.mapToObj(i -> {
					ColumnConstraints colC = new ColumnConstraints();
					colC.setPercentWidth(percentWidth);
					if (i == 0) {
						colC.setHalignment(HPos.LEFT);
					} else if (i < columnCount) {
						colC.setHalignment(HPos.LEFT);
					} else {
						colC.setHalignment(HPos.RIGHT);
					}
					return colC;
				})
				.forEach(c -> grid.getColumnConstraints().add(c));
		return grid;
	}

	private static final IntegerStringConverter SCORE_STRING_CONVERTER = new IntegerStringConverter();

	public static TextField createScoreField(int initialValue) {
		TextField field = new TextField();
		field.setTextFormatter(new TextFormatter<>(
				SCORE_STRING_CONVERTER,
				Score.DEFAULT_VALUE,
				change -> {
					String newValue = change.getControlNewText();
					if (!newValue.isEmpty()) {
						Integer score = SCORE_STRING_CONVERTER.fromString(newValue);
						if (score != null) {
							if (score >= Score.MIN_VALUE && score <= Score.MAX_VALUE) {
								return change;
							}
						}
					}
					return null;
				}
		));
		field.setText(SCORE_STRING_CONVERTER.toString(initialValue));
		field.setAlignment(Pos.CENTER_RIGHT);
		field.setPrefWidth(30);
		field.setMaxWidth(30);
		return field;
	}

}
