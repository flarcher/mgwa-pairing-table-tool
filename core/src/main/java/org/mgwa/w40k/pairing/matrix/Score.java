package org.mgwa.w40k.pairing.matrix;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A matrix score.
 * Between 2 armies, the score must be between 0 and 20.
 */
public class Score implements Consumer<Score> {

	public static Score newDefault() {
		return new Score(MIN_VALUE, MAX_VALUE);
	}

	private static void checkValueBound(int value) {
		if (value < MIN_VALUE || value > MAX_VALUE) {
			throw new IllegalArgumentException("Out of bounds score value");
		}
	}

	public Score(int min, int max) {
		checkValueBound(min);
		checkValueBound(max);
		this.min = min;
		this.max = max;
		this.sum = min + max;
		this.count = 1;
	}

	public static final int MIN_VALUE = 0;
	public static final int MAX_VALUE = 20;
	static {
		assert MIN_VALUE < MAX_VALUE;
	}
	public static final int MEDIUM_SCORE_VALUE = (Score.MAX_VALUE - Score.MIN_VALUE) / 2;
	public static final int DEFAULT_VALUE = 0;
	static {
		assert DEFAULT_VALUE >= MIN_VALUE;
		assert DEFAULT_VALUE <= MAX_VALUE;
	}

	private int min;
	private int max;
	private int sum;
	private int count;

	@Override
	public void accept(Score score) {
		this.min = Math.min(this.min, score.min);
		this.max = Math.max(this.max, score.max);
		this.sum += (score.min + score.max);
		this.count++;
	}

	private void updateSum(int oldMin, int oldMax) {
		this.sum -= (oldMin + oldMax);
		this.sum += (this.min + this.max);
	}

	public int getMinValue() {
		return this.min;
	}

	public Score updateMinValue(int newMinValue) {
		checkValueBound(newMinValue);
		int oldMin = this.min;
		int oldMax = this.max;
		this.min = newMinValue;
		if (this.max < newMinValue) {
			this.max = newMinValue;
		}
		updateSum(oldMin, oldMax);
		return this;
	}

	public int getMaxValue() {
		return this.max;
	}

	public Score updateMaxValue(int newMaxValue) {
		checkValueBound(newMaxValue);
		int oldMin = this.min;
		int oldMax = this.max;
		this.max = newMaxValue;
		if (this.min > newMaxValue) {
			this.min = newMaxValue;
		}
		updateSum(oldMin, oldMax);
		return this;
	}

	public int getAverage() {
		return this.sum / (2 * this.count);
	}
	public int getCount() {
		return count;
	}

	@Override
	public String toString() {
		return String.format("%d-%d", this.min, this.max);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Score score = (Score) o;
		return min == score.min && max == score.max && sum == score.sum && count == score.count;
	}

	@Override
	public int hashCode() {
		return Objects.hash(min, max, sum, count);
	}
}
