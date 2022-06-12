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
		public Integer getIdentityScore() {
			return 0;
		}

		@Override
		public BinaryOperator<Integer> getScoreReducer() {
			return Math::max;
		}

		@Override
		public int getFinalizer(int reducedScore, int count) {
			return reducedScore;
		}
	},
	/**
	 * Uses the pairing that maximizes the average of scores.
	 */
	AVERAGE {
		@Override
		public Integer getIdentityScore() {
			return 0;
		}

		@Override
		public BinaryOperator<Integer> getScoreReducer() {
			return Integer::sum;
		}

		@Override
		public int getFinalizer(int reducedScore, int count) {
			return reducedScore / count;
		}
	},

	;

	public abstract Integer getIdentityScore();
	public abstract BinaryOperator<Integer> getScoreReducer();
	public abstract int getFinalizer(int reducedScore, int count);
}
