package org.mgwa.w40k.pairing;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A pair linked to a related score.
 * Note: the score can be outside the allowed range defined in {@link org.mgwa.w40k.pairing.matrix.Score} but should eventually have an acceptable value in this range.
 */
public class ScoredPair implements
		Comparable<ScoredPair>,
		Consumer<ScoredPair> {

	private static final Comparator<ScoredPair> SCORED_PAIR_COMPARATOR = Comparator.<ScoredPair>
			comparingInt(sp -> sp.pair.getRow())
			.thenComparingInt(sp -> sp.pair.getColumn())
			// Then, the pairs are the same: thus, we can compare the score
			.thenComparingInt(sp -> sp.score);

	ScoredPair(@Nonnull Pair pair) {
		this.pair = Objects.requireNonNull(pair);
		this.score = 0;
	}

	private final Pair pair;
	private int score = 0;

	public Pair getPair() {
		return pair;
	}

	public int getScore() {
		return score;
	}

	public ScoredPair setScore(int score) {
		this.score = score;
		return this;
	}

	@Override
	public String toString() {
		return String.format("[%d,%d]=%d", pair.getRow(), pair.getColumn(), score);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ScoredPair that = (ScoredPair) o;
		return SCORED_PAIR_COMPARATOR.compare(this, that) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(pair, score);
	}

	@Override
	public int compareTo(ScoredPair o) {
		return SCORED_PAIR_COMPARATOR.compare(this, o);
	}

	public ScoredPair addScore(int add) {
		score += add;
		return this;
	}

	private boolean addScore(ScoredPair scoredPair) {
		if (scoredPair.getPair().equals(pair)) {
			addScore(scoredPair.score);
			return true;
		}
		return false;
	}

	@Override
	public void accept(ScoredPair scoredPair) {
		addScore(scoredPair);
	}
}
