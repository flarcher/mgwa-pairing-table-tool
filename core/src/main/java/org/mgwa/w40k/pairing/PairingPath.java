package org.mgwa.w40k.pairing;

import org.mgwa.w40k.pairing.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class about the discovery of possible pairing paths.
 */
class PairingPath {

	PairingPath(Collection<Pair> pairs) {
		this.orderedPath = pairs.stream()
			.mapToInt(Pair::uniqueValue)
			.sorted()
			.toArray();
		this.pairs = pairs;
	}

	PairingPath(Pair pair) {
		this.orderedPath = new int[]{ pair.uniqueValue() };
		this.pairs = Collections.singletonList(pair);
	}

	private int[] orderedPath;
	private final Collection<Pair> pairs;

	public Collection<Pair> getPairs() {
		return pairs;
	}

	int[] getSortedValues() {
		return orderedPath;
	}

	PairingPath addPair(Pair pair) {
		pairs.add(pair);
		int[] newValues = Arrays.copyOf(orderedPath, pairs.size());
		newValues[pairs.size() - 1] = pair.uniqueValue();
		Arrays.sort(newValues);
		orderedPath = newValues;
		return this;
	}

	public int length() {
		return orderedPath.length;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PairingPath that = (PairingPath) o;
		return Arrays.equals(orderedPath, that.orderedPath);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(orderedPath);
	}

	@Override
	public String toString() {
		return pairs.stream().map(Object::toString).collect(Collectors.joining("-"));
	}

	static Collection<PairingPath> getPossiblePaths(Collection<Pair> possiblePairs, PairingState state, boolean filterRedundantPath) {
		switch (state.getAssignLeftCount()) {
			case 0:
				return new ArrayList<>(0);
			case 1: {
				Pair lastPair = CollectionUtils.getOneAndOnly(possiblePairs.stream().filter(state));
				ArrayList<Pair> pairs = new ArrayList<>(state.getSize());
				pairs.add(lastPair);
				ArrayList<PairingPath> result = new ArrayList<>(1);
				result.add(new PairingPath(pairs));
				return result;
			}
			case 2: {
				return getAllPossiblePaths(possiblePairs, state)
					.collect(Collectors.toList()); // A list is enough (no redundancy)
			}
			default: { // > 2
				Stream<PairingPath> allPossiblePaths = getAllPossiblePaths(possiblePairs, state);
				return filterRedundantPath
					? allPossiblePaths.collect(Collectors.toSet()) // A set is needed for filtering of redundant paths
					: allPossiblePaths.collect(Collectors.toList()); // Returning several path of same pair in different orders
			}
		}
	}

	private static Stream<PairingPath> getAllPossiblePaths(Collection<Pair> possiblePairs, PairingState state) {
		Stream.Builder<PairingPath> resultBuilder = Stream.builder();
		possiblePairs.stream()
			.filter(state)
			.forEach(startPair -> {
				PairingState newState = state.cloneIt().assign(startPair);
				getPossiblePaths(possiblePairs, newState, false)
					.stream()
					.map(path -> path.addPair(startPair))
					.forEach(resultBuilder::add);
			});
		return resultBuilder.build();
	}

}
