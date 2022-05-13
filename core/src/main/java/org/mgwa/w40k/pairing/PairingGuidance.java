package org.mgwa.w40k.pairing;

import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.Score;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Contains the algorithm that suggest the best pairing.
 */
public final class PairingGuidance {

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

	// Inputs and parameters
	private final Matrix matrix;
	/** Policy about the reading of the score */
	private ScoreReading scoreReading = ScoreReading.MITIGATED;
	/** How the future paths will be considered */
	private ForecastMethod forecastMethod = ForecastMethod.AVERAGE;
	private boolean filterRedundantPath = true;
	/** Filter for next pairs */
	private Predicate<Pair> nextPairFilter = p -> true;

	public Matrix getMatrix() {
		return matrix;
	}

	public ScoreReading getScoreReading() {
		return scoreReading;
	}

	public PairingGuidance setScoreReading(ScoreReading scoreReading) {
		this.scoreReading = Objects.requireNonNull(scoreReading);
		return this;
	}

	public ForecastMethod getForecastMethod() {
		return forecastMethod;
	}

	public PairingGuidance setForecastMethod(ForecastMethod forecastMethod) {
		this.forecastMethod = Objects.requireNonNull(forecastMethod);
		return this;
	}

	public boolean isFilterRedundantPath() {
		return filterRedundantPath;
	}

	public PairingGuidance setFilterRedundantPath(boolean filterRedundantPath) {
		this.filterRedundantPath = filterRedundantPath;
		return this;
	}

	public Predicate<Pair> getNextPairFilter() {
		return nextPairFilter;
	}

	public PairingGuidance setNextPairFilter(Predicate<Pair> nextPairFilter) {
		this.nextPairFilter = Objects.requireNonNull(nextPairFilter);
		return this;
	}

	// Logging
	private final Optional<Consumer<String>> debugger;

	private void debug(String template, Object... args) {
        debug(() -> String.format(template, args));
    }

	private void debug(Supplier<String> msg) {
        debugger.ifPresent(debugger -> debugger.accept(msg.get()));
    }

	// Algorithm
	private static final Comparator<ScoredPair> SCORE_COMPARATOR = Comparator
			.comparingInt(ScoredPair::getScore) // Score is compared at first
			.reversed() // Descending order
			// Then, make sure that it is not equal for different pairs
			.thenComparingInt(sp -> sp.getPair().getRow())
			.thenComparingInt(sp -> sp.getPair().getColumn());

	/**
	 * @param assignment Current tables assignment.
	 * @return A sorted suggestion for the next pair to be considered.
	 */
	public SortedSet<ScoredPair> suggestPairing(Assignment assignment) {
		return suggestPairing(assignment.possiblePairs(), assignment.getUnassignedTableCount());
	}

	/**
	 * @param possiblePairs All possible pairs to sort.
	 * @param remainingIteration Remaining assignment count.
	 * @return A sorted suggestion for the next pair to be considered.
	 */
	public SortedSet<ScoredPair> suggestPairing(
			Collection<Pair> possiblePairs,
			int remainingIteration) {

		switch (remainingIteration) {
			case 0:
				return Collections.emptySortedSet();
			case 1: {
				SortedSet<ScoredPair> result = new TreeSet<>(SCORE_COMPARATOR);
				possiblePairs.stream()
						.filter(nextPairFilter)
						.map(p -> new ScoredPair(p).setScore(getScore(scoreReading, p)))
						.forEach(result::add);
				return Collections.unmodifiableSortedSet(result);
			}
			default:
				// See below
		}

		debug("Start with %d pairs, %d depth, %s scoring, %s method",
				possiblePairs.size(), remainingIteration, scoreReading, forecastMethod);
		SortedSet<ScoredPair> result = new TreeSet<>(SCORE_COMPARATOR);
		PairingState state = new PairingState(remainingIteration);
		possiblePairs.stream()
			.filter(nextPairFilter)
			.forEach(nextPair -> {
				int firstScore = getScore(scoreReading, nextPair);
				debug("- %s => %d + ?", nextPair, firstScore);
				PairingState newState = state.cloneIt().assign(nextPair);
				Collection<PairingPath> possiblePaths = PairingPath.getPossiblePaths(possiblePairs, newState, filterRedundantPath);
				int reducedScore = getReducedScore(possiblePaths, remainingIteration, firstScore);
				int finalScore = forecastMethod.getFinalizer(reducedScore, remainingIteration);
				finalScore /= remainingIteration;
				debug("- %s => %d", nextPair, finalScore);
				result.add(new ScoredPair(nextPair).setScore(finalScore));
			});
		return Collections.unmodifiableSortedSet(result);
	}

	private int getReducedScore(Collection<PairingPath> possiblePaths, int remainingIteration, int add) {
		switch (remainingIteration) {
			case 0:
				return Score.DEFAULT_VALUE;
			case 1:
				// TODO
		}
		int reducedScore = possiblePaths.stream()
			.map(path -> {
				// TODO: make recursive call
				int pathScore = path.getPairs().stream()
					.map(p -> getScore(scoreReading, p))
					.reduce(0, Integer::sum);
				debug(() -> String.format("-- %s => %d", path, pathScore));
				return add + pathScore;
			})
			.reduce(forecastMethod.getIdentityScore(), forecastMethod.getScoreReducer());
		return reducedScore;
	}

	private int getScore(ScoreReading scoreReading, Pair pair) {
		Optional<Score> score = matrix.getScore(pair.getRow(), pair.getColumn());
		return scoreReading.readScore(score.orElseThrow(
			() -> new IllegalStateException(
				String.format("No score at %d-%d", pair.getRow(), pair.getColumn()))));
	}

}
