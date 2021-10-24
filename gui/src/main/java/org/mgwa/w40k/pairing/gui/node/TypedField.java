package org.mgwa.w40k.pairing.gui.node;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;

/**
 * A field that can handle a value of a type different than {@link String}.
 * @param <T> The type represented in the text field.
 */
public abstract class TypedField<T> extends TextField {

	public TypedField(T value) {
		super();
		initHandlers();
		setValue(value);
	}

	public T getValue() {
		return property.getValue();
	}

	public void setValue(T property) {
		this.property.setValue(property);
	}

	private final ObjectProperty<T> property = new SimpleObjectProperty<>();

	private void initHandlers() {

		// try to parse when focus is lost or RETURN is hit
		setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				parseAndFormatInput();
			}
		});

		focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (!newValue.booleanValue()) {
					parseAndFormatInput();
				}
			}
		});

		// Set text in field if BigDecimal property is changed from outside.
		property.addListener(new ChangeListener<T>() {

			@Override
			public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
				setText(toText(newValue));
			}
		});
	}

	private void parseAndFormatInput() {
		String input = getText();
		if (input == null || input.length() == 0) {
			return;
		}
		setValue(fromText(input));
		selectAll();
	}

	protected abstract T fromText(String input);
	protected abstract String toText(T value);
}
