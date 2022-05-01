package org.mgwa.w40k.pairing;

import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.Score;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Contains the algorithm that suggest the best pairing.
 */
public final class PairingGuidance {

	private static final int DEFAULT_SCORE = 0;

	public PairingGuidance(@Nonnull Matrix matrix) {
		this(matrix, Optional.empty());
	}

	public PairingGuidance(@Nonnull Matrix matrix, Consumer<String> debug) {
		this(matrix, Optional.of(debug));
	}

	private PairingGuidance(@Nonnull Matrix matrix, Optional<Consumer<String>> debugger) {
		this.matrix = Objects.requireNonNull(matrix);
		if (!matrix.isComplete()) {
			throw new IllegalArgumentException("Some score is missing in the matrix");
		}
		this.debugger = debugger;
	}

	private final Matrix matrix;
	private final Optional<Consumer<String>> debugger;

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

    private static final Comparator<ScoredPair> SCORED_PAIR_COMPARATOR = Comparator.<ScoredPair>
            comparingInt(sp -> sp.pair.getRow())
            .thenComparingInt(sp -> sp.pair.getColumn())
			// Then, the pairs are the same: thus, we can compare the score
            .thenComparingInt(sp -> sp.score);

	private static final Comparator<ScoredPair> SCORE_COMPARATOR = Comparator
			.comparingInt(ScoredPair::getScore)
			// Then, make sure that it is not equal for different pairs
			.thenComparingInt(sp -> sp.pair.getRow())
			.thenComparingInt(sp -> sp.pair.getColumn());

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

	private void debug(String template, Object... args) {
		debug(() -> String.format(template, args));
	}

	private void debug(Supplier<String> msg) {
		debugger.ifPresent(debugger -> debugger.accept(msg.get()));
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

		//String pairs = possiblePairs.stream().map(Object::toString).collect(Collectors.joining(","));
		debug("Start with %d pairs, %d depth, %s scoring, %s method",
				possiblePairs.size(), remainingIteration, scoreReading, method);
		SortedSet<ScoredPair> result = new TreeSet<>(SCORE_COMPARATOR);
		possiblePairs.stream()
			.filter(nextPairFilter)
			.forEach(nextPair -> {
				debug("- (%d) %s => ?", remainingIteration, nextPair);
				Predicate<Pair> pairFilter = Pair
						.afterAssignmentOf(nextPair)
						.or(Predicate.isEqual(nextPair));
				int score = getScore(scoreReading, possiblePairs, pairFilter, method, remainingIteration);
				debug("- (%d) %s => %d", remainingIteration, nextPair, score);
				result.add(new ScoredPair(nextPair).setScore(score));
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
				int score = getScore(scoreReading, lastPair);
				debug("--- (1) %s => %d", lastPair, score);
				return score;
			}
			default: {
				List<ScoredPair> scoredPairs = getScoredPairs(scoreReading,
						possiblePairs, pairFilter,
						method, remainingIteration);
				return reduceScore(scoredPairs, method);
			}
		}
	}

	private List<ScoredPair> getScoredPairs(ScoreReading scoreReading,
		Collection<Pair> possiblePairs, Predicate<Pair> pairFilter,
		ForecastMethod method, int remainingIteration) {

		return possiblePairs.stream()
			.filter(pairFilter)
			.map(pair -> {
				int score = getScore(scoreReading, pair);
				Predicate<Pair> nextPairsPredicate = pairFilter.and(Pair.afterAssignmentOf(pair));
				// Recursive call
				List<ScoredPair> nextScoredPairs = getScoredPairs(scoreReading,
						possiblePairs, nextPairsPredicate, method, remainingIteration - 1);
				score += reduceScore(nextScoredPairs, method);
				debug("-- (%d) %s => %d", remainingIteration, pair, score);
				return new ScoredPair(pair).setScore(score);
			})
			.sorted(SCORE_COMPARATOR)
			.collect(Collectors.toList());
	}

	private static int reduceScore(List<ScoredPair> scoredPairs, ForecastMethod method) {
		return scoredPairs.stream()
				.map(ScoredPair::getScore)
				.reduce(method.getIdentityScore(), method.getScoreReducer());
	}
}
