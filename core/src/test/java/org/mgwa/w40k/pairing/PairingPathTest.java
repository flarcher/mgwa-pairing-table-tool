package org.mgwa.w40k.pairing;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PairingPathTest {

	private void testPossiblePath(int indexCount) {
		List<Integer> indexes = IntStream.range(0, indexCount).boxed().collect(Collectors.toList());
		Collection<Pair> pairs = Pair.possiblePairs(indexes, indexes);
		List<Collection<Pair>> possiblePaths = PairingPath.getPossiblePaths(pairs, new PairingState(indexCount)).collect(Collectors.toList());
		System.out.println(possiblePaths.stream()
				.map(path -> path.stream().map(Objects::toString).collect(Collectors.joining("")))
				.collect(Collectors.joining("|")));
		indexes.forEach(i -> {
			Assert.assertTrue(possiblePaths.stream().anyMatch(
					path -> path.stream().anyMatch(
							p -> p.getRow() == i || p.getColumn() == i)));
		});
		possiblePaths.forEach(path -> Assert.assertEquals(indexCount, path.size()));
		Assert.assertEquals(indexCount * indexCount, possiblePaths.size());
	}

	@Test
	public void testPossiblePath_2() {
		testPossiblePath(2);
	}

	@Test
	public void testPossiblePath_3() {
		testPossiblePath(3);
	}

}
