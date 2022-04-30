package org.mgwa.w40k.pairing;

import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * Defines how to anticipate next pairings
 */
public enum ForecastMethod {

	/**
	 * Uses the pairing that has the maximum score.
	 */
	LUCKY_BUT_RISKY {
		@Override
		public BinaryOperator<Integer> getScoreReducer() {
			return Math::max;
		}

		@Override
		public Function<Integer, Integer> getFinalizer(int pairCount) {
			return Function.identity();
		}
	},
	/**
	 * Uses the pairing that maximizes the average of scores.
	 */
	AVERAGE {
		@Override
		public BinaryOperator<Integer> getScoreReducer() {
			return Integer::sum;
		}

		@Override
		public Function<Integer, Integer> getFinalizer(int pairCount) {
			return score -> score / pairCount;
		}
	},

	;

	public abstract BinaryOperator<Integer> getScoreReducer();
	public abstract Function<Integer, Integer> getFinalizer(int pairCount);
}
