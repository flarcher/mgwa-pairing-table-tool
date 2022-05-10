package org.mgwa.w40k.pairing;

import java.util.function.BinaryOperator;

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
			return (l, r) -> (l + r) / 2;
		}

	},

	;

	public abstract Integer getIdentityScore();
	public abstract BinaryOperator<Integer> getScoreReducer();

}
