package org.mgwa.w40k.pairing.matrix;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A matrix score.
 * Between 2 armies, the score must be between 0 and 20.
 */
public class Score implements Consumer<Score>, Cloneable {

	public static Score newDefault() {
		return of(MIN_VALUE, MAX_VALUE);
	}

	public static Score of(int min, int max) {
		return new Score(min, max);
	}

	private static void checkValueBound(int value) {
		if (value < MIN_VALUE || value > MAX_VALUE) {
			throw new IllegalArgumentException("Out of bounds score value");
		}
	}

	private Score(int min, int max) {
		checkValueBound(min);
		checkValueBound(max);
		this.min = min;
		this.max = max;
		this.sum = min + max;
		this.count = 1;
	}

	private Score(Score other) {
		this.min = other.min;
		this.max = other.max;
		this.sum = other.sum;
		this.count = other.count;
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
		if (this.max < newMinValue) {
			this.max = newMinValue;
		}
		else {
			this.min = newMinValue;
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
		if (this.min > newMaxValue) {
			this.min = newMaxValue;
		}
		else {
			this.max = newMaxValue;
		}
		updateSum(oldMin, oldMax);
		return this;
	}

	public float getAverage() {
		return ((float) this.sum) / (2 * this.count);
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

	public Score cloneIt() {
		return new Score(this);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return cloneIt();
	}
}
