package flarcher.pairing;

import flarcher.pairing.matrix.Matrix;

import java.util.Comparator;
import java.util.Set;

public enum Strategy {

	/**
	 * Uses the pairing that allows the path with the maximum of score.
	 */
	RISKY {
		@Override
		Comparator<Army> comparator(Matrix estimates, Set<Assignment> combinations) {
			return null;
		}
	},
	/**
	 * Uses the pairing that maximizes the average of possible scores.
	 */
	FLAT {
		@Override
		Comparator<Army> comparator(Matrix estimates, Set<Assignment> combinations) {
			return null;
		}
	},
	/**
	 * Uses the pairing that avoids the path with the minimum score.
	 */
	SECURED {
		@Override
		Comparator<Army> comparator(Matrix estimates, Set<Assignment> combinations) {
			return null;
		}
	},

	;

	abstract Comparator<Army> comparator(Matrix estimates, Set<Assignment> combinations);

}
