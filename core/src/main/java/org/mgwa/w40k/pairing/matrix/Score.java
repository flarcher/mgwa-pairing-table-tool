package org.mgwa.w40k.pairing.matrix;

import java.util.Objects;

/**
 * A matrix score.
 * Between 2 armies, the score must be between 0 and 20.
 */
public class Score {

	public Score() {
		this.sum = 0;
		this.count = 0;
	}

	public Score(int value) {
		this();
		addValue(value);
	}

	public static final int NO_VALUE = -1;
	public static final int MIN_VALUE = 0;
	public static final int MAX_VALUE = 20;
	static {
		assert MIN_VALUE < MAX_VALUE;
		assert NO_VALUE < MIN_VALUE || NO_VALUE > MAX_VALUE;
	}

	private int sum;
	private int count;

	public Score addValue(int value) {
		if (value < MIN_VALUE || value > MAX_VALUE) {
			throw new IllegalArgumentException("Bad score value " + value);
		}
		sum += value;
		count++;
		return this;
	}

	public Score reset() {
		this.sum = 0;
		this.count = 0;
		return this;
	}

	public Score setValue(int value) {
		this.sum = value;
		this.count = 0;
		return this;
	}

	public int getValue() {
		return count == 0 ? NO_VALUE : sum / count;
	}

	public int getCount() {
		return count;
	}

	@Override
	public String toString() {
		return Integer.toString(getValue());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Score score = (Score) o;
		return sum == score.sum &&
				count == score.count;
	}

	@Override
	public int hashCode() {
		return Objects.hash(sum, count);
	}
}
