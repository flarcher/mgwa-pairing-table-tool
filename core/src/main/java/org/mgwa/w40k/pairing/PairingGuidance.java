package org.mgwa.w40k.pairing;

import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.Score;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Contains the algorithm that suggest the best pairing.
 */
public final class PairingGuidance {

	private static final int DEFAULT_SCORE = 0;

	public PairingGuidance(@Nonnull Matrix matrix) {
		this.matrix = Objects.requireNonNull(matrix);
		if (!matrix.isComplete()) {
			throw new IllegalArgumentException("Some score is missing in the matrix");
		}
	}

	private final Matrix matrix;

    public static class ScoredPair implements
			Comparable<ScoredPair>,
			Consumer<ScoredPair> {

        private ScoredPair(@Nonnull Pair pair) {
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

        private ScoredPair setScore(int score) {
            this.score = score;
			return this;
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

		private ScoredPair addScore(int add) {
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

    private static final Comparator<ScoredPair> SCORED_PAIR_COMPARATOR = Comparator.<ScoredPair>
            comparingInt(sp -> sp.pair.getRow())
            .thenComparingInt(sp -> sp.pair.getColumn())
			// Then, the pairs are the same: thus, we can compare the score
            .thenComparingInt(sp -> sp.score);

	public static Predicate<Pair> afterAssignmentOf(Pair pair) {
		return p -> p.getColumn() != pair.getColumn()
		         && p.getRow()    != pair.getRow();
	}

    /**
     * @param scoreReading Policy about the reading of the score.
     * @param assignment Current tables assignment.
     * @param nextPairFilter Filters for next pairs.
     * @param method How the future paths will be considered.
     * @return A sorted suggestion for the next pair to be considered.
     */
    public SortedSet<ScoredPair> suggestPairing(
            ScoreReading scoreReading,
            Assignment assignment,
            Predicate<Pair> nextPairFilter,
            ForecastMethod method) {

        return suggestPairing(scoreReading,
				assignment.possiblePairs(),
				nextPairFilter,
				method,
				assignment.getUnassignedTableCount());
    }

    /**
     * @param scoreReading Policy about the reading of the score.
     * @param possiblePairs All possible pairs to sort.
     * @param nextPairFilter Filters for next possible pairs.
     * @param method How the future paths will be considered.
     * @return A sorted suggestion for the next pair to be considered.
     */
    public SortedSet<ScoredPair> suggestPairing(
            ScoreReading scoreReading,
            Collection<Pair> possiblePairs,
            Predicate<Pair> nextPairFilter,
            ForecastMethod method,
			int remainingIteration) {

		SortedSet<ScoredPair> result = new TreeSet<>(SCORED_PAIR_COMPARATOR);
		possiblePairs.stream()
			.filter(nextPairFilter)
			.forEach(nextPair -> {
				Predicate<Pair> pairFilter = afterAssignmentOf(nextPair);
				int score = getScore(scoreReading, possiblePairs, pairFilter, method, remainingIteration);
				ScoredPair scoredPair = new ScoredPair(nextPair);
				scoredPair.setScore(score);
				result.add(scoredPair);
			});
        return result;
    }

	private int getScore(ScoreReading scoreReading, Pair pair) {
		Optional<Score> score = matrix.getScore(pair.getRow(), pair.getColumn());
		return scoreReading.readScore(score.orElseThrow(
			() -> new IllegalStateException(
				String.format("No score at %d-%d", pair.getRow(), pair.getColumn()))));
	}

	private int getScore(
		ScoreReading scoreReading,
		Collection<Pair> possiblePairs,
		Predicate<Pair> pairFilter,
		ForecastMethod method,
		int remainingIteration) {

		// First, consider trivial cases
		switch (remainingIteration) {
			case 0:
				return DEFAULT_SCORE;
			case 1: {
				// TODO: check that there is at most one pair
				Pair lastPair = possiblePairs.stream()
						.filter(pairFilter).findAny().orElseThrow(
								() -> new IllegalStateException("Remains 1 iteration but 0 pair"));
				int lastPairScore = getScore(scoreReading, lastPair);
				return method.getFinalizer(remainingIteration).apply(lastPairScore);
			}
			default:
				// See below
		}

		List<ScoredPair> scoredPairs = getScoredPairs(scoreReading,
			possiblePairs, pairFilter,
			method, remainingIteration);
		int methodScore = scoredPairs.stream()
			.map(ScoredPair::getScore)
			.reduce(0, method.getScoreReducer());
		return method.getFinalizer(remainingIteration).apply(methodScore);
	}

	private List<ScoredPair> getScoredPairs(ScoreReading scoreReading,
		Collection<Pair> possiblePairs, Predicate<Pair> pairFilter,
		ForecastMethod method, int remainingIteration) {

		return possiblePairs.stream()
			.filter(pairFilter)
			.map(pair -> {
				int score = getScore(scoreReading, pair);
				Predicate<Pair> nextPairsPredicate = pairFilter.and(afterAssignmentOf(pair));
				List<ScoredPair> nextScoredPairs = getScoredPairs(scoreReading,
						possiblePairs, nextPairsPredicate, method, remainingIteration - 1);
				score += nextScoredPairs.stream()
						.map(ScoredPair::getScore)
						.reduce(DEFAULT_SCORE, method.getScoreReducer());
				return new ScoredPair(pair).setScore(score);
			})
			.sorted(SCORED_PAIR_COMPARATOR)
			.collect(Collectors.toList());
	}

}
