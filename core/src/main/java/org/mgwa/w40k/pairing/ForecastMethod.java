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
		public boolean toBeDivided() {
			return true;
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

		@Override
		public boolean toBeDivided() {
			return false;
		}
	},

	;

	public abstract Integer getIdentityScore();
	public abstract BinaryOperator<Integer> getScoreReducer();

	public abstract boolean toBeDivided();
}
