package org.mgwa.w40k.pairing.gui.node;

public class IntegerRangeField extends TypedField<Integer> {

	private final int minimum;
	private final int maximum;

	public IntegerRangeField(int value, int minimum, int maximum) {
		super(value);
		this.minimum = minimum;
		this.maximum = maximum;
	}

	private int inRange(int value) {
		return Math.max(Math.min(value, maximum), minimum);
	}

	@Override
	protected Integer fromText(String input) {
		if (input == null || input.isEmpty()) {
			return minimum;
		}
		try {
			return inRange(Integer.parseInt(input));
		}
		catch (NumberFormatException e) {
			return minimum;
		}
	}

	@Override
	protected String toText(Integer value) {
		if (value == null) {
			return Integer.toString(minimum);
		}
		int newValue = inRange(value);
		return Integer.toString(newValue);
	}
}
