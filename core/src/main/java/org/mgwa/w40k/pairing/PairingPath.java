package org.mgwa.w40k.pairing;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class about the discovery of possible pairing paths.
 */
final class PairingPath {

	private PairingPath() { } // Utility class


	public static Stream<Set<Pair>> getPossiblePaths(Collection<Pair> possiblePairs, PairingState state) {
		switch (state.getAssignLeftCount()) {
			case 0:
				return Stream.empty();
			case 1: {
				Pair lastPair = getOneAndOnly(possiblePairs.stream().filter(state));
				Set<Pair> set = new HashSet<>(state.getSize());
				set.add(lastPair);
				return Stream.of(set);
			}
			case 2: {
				return getAllPossiblePaths(possiblePairs, state);
			}
			default: {
				Stream.Builder<Set<Pair>> resultBuilder = Stream.builder();
				possiblePairs.stream()
					.filter(state)
					.forEach(startPair -> {
						PairingState newState = state.cloneIt().assign(startPair);
						List<Collection<Pair>> possiblePaths = getAllPossiblePaths(possiblePairs, newState).collect(Collectors.toList());
						possiblePaths.stream()
							//.filter(c -> possiblePaths.stream().noneMatch(c::containsAll))
							.map(c -> {
								Set<Pair> newSet = new HashSet<>(c);
								newSet.add(startPair);
								return newSet;
							})
							.forEach(resultBuilder::add);
					});
				// TODO: add filter: 36 au lieu de 9 à cause de l'ordre ?
				return resultBuilder.build();
			}
		}
	}
	private static Stream<Set<Pair>> getAllPossiblePaths(Collection<Pair> possiblePairs, PairingState state) {
		Stream.Builder<Set<Pair>> resultBuilder = Stream.builder();
		possiblePairs.stream()
				.filter(state)
				.forEach(startPair -> {
					PairingState newState = state.cloneIt().assign(startPair);
					List<Collection<Pair>> possiblePaths = getPossiblePaths(possiblePairs, newState).collect(Collectors.toList());
					possiblePaths.stream()
							.map(c -> {
								Set<Pair> newSet = new HashSet<>(c);
								newSet.add(startPair);
								return newSet;
							})
							.forEach(resultBuilder::add);
				});
		return resultBuilder.build();
	}

	private static <T> T getOneAndOnly(Stream<T> stream) {
		Iterator<T> iterator = stream.iterator();
		if (!iterator.hasNext()) {
			throw new IllegalStateException("0 item");
		}
		T item = iterator.next();
		if (iterator.hasNext()) {
			throw new IllegalStateException("Several items");
		}
		return item;
	}

}
