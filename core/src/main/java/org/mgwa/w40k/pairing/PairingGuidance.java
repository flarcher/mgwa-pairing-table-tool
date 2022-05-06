package org.mgwa.w40k.pairing;

import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.Score;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

	private final Matrix matrix;
	private final Optional<Consumer<String>> debugger;


	private void debug(String template, Object... args) {
        debug(() -> String.format(template, args));
    }

	private void debug(Supplier<String> msg) {
        debugger.ifPresent(debugger -> debugger.accept(msg.get()));
    }

	private static final Comparator<ScoredPair> SCORE_COMPARATOR = Comparator
			.comparingInt(ScoredPair::getScore) // Score is compared at first
			.reversed() // Descending order
			// Then, make sure that it is not equal for different pairs
			.thenComparingInt(sp -> sp.getPair().getRow())
			.thenComparingInt(sp -> sp.getPair().getColumn());

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

		//String pairs = possiblePairs.stream().map(Object::toString).collect(Collectors.joining(","));
		debug("Start with %d pairs, %d depth, %s scoring, %s method",
				possiblePairs.size(), remainingIteration, scoreReading, method);
		SortedSet<ScoredPair> result = new TreeSet<>(SCORE_COMPARATOR);
		possiblePairs.stream()
			.filter(nextPairFilter)
			.forEach(nextPair -> {
				int score = getScore(scoreReading, nextPair);
				debug("- %s => %d + ?", nextPair, score);
				PairingState state = new PairingState(remainingIteration).assign(nextPair);
				score += getScore(scoreReading, possiblePairs, method, state); // here use the method
				score /= (1 /* this assignment */ + state.getCloneCount());
				debug("- %s => %d", nextPair, score);
				result.add(new ScoredPair(nextPair).setScore(score));
			});
		return Collections.unmodifiableSortedSet(result);
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
		ForecastMethod method,
		PairingState state) {

		// First, consider trivial cases
		switch (state.getAssignLeftCount()) {
			case 0:
				debug("---- (0) ø");
				throw new IllegalStateException("Remains 0 iteration?");
			case 1: {
				Iterator<Pair> iterator = possiblePairs.stream().filter(state).iterator();
				if (!iterator.hasNext()) {
					throw new IllegalStateException("Remains 1 iteration but 0 pair");
				}
				Pair lastPair = iterator.next();
				if (iterator.hasNext()) {
					throw new IllegalStateException("Remains 1 iteration but several pairs");
				}
				int score = getScore(scoreReading, lastPair);
				//state.assign(lastPair); // Not needed
				debug("--- (1) %s => %d", lastPair, score);
				return score;
			}
			default: {
				return possiblePairs.stream()
					.filter(state)
					.map(pair -> {
						int score = getScore(scoreReading, pair);
						PairingState newState = state.cloneIt().assign(pair);
						debug("-- (%d) %s => %d + ?", state.getAssignLeftCount(), pair, score);
						//-- Recursive call
						score += getScore(scoreReading, possiblePairs, method, newState); // here use the method
						//--
						debug("-- (%d) %s => %d", state.getAssignLeftCount(), pair, score);
						return score;
					})
					.reduce(0, Integer::sum);
			}
		}
	}
}
