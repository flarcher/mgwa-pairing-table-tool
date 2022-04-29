package org.mgwa.w40k.pairing;

/**
 * Defines how to anticipate next pairings
 */
public enum ForecastMethod {

	/**
	 * Uses the pairing that has the maximum score.
	 */
	LUCKY_BUT_RISKY,
	/**
	 * Uses the pairing that maximizes the average of scores.
	 */
	AVERAGE,

}
