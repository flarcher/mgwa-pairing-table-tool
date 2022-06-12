package org.mgwa.w40k.pairing;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PairingPathTest {

	@Test
	public void testPathValuesOrdering() {
		PairingPath path = new PairingPath(Arrays.asList(
				Pair.of(2,2),
				Pair.of(1,1),
				Pair.of(0,0)
			));
		int[] orderedValues = path.getSortedValues();
		//System.out.println(Arrays.toString(orderedValues));
		Assert.assertEquals(path.getPairs().size(), orderedValues.length);
		Assert.assertEquals(0, orderedValues[0]);
		Assert.assertEquals(32769, orderedValues[1]);
		Assert.assertEquals(65538, orderedValues[2]);
		PairingPath reversed = new PairingPath(Arrays.asList(
				Pair.of(0,0),
				Pair.of(1,1),
				Pair.of(2,2)
			));
		int[] newOrderedValues = reversed.getSortedValues();
		//System.out.println(Arrays.toString(newOrderedValues));
		Assert.assertArrayEquals(orderedValues, newOrderedValues);
	}

	private void testPossiblePath(int indexCount, int expectedPathCount) {
		List<Integer> indexes = IntStream.range(0, indexCount).boxed().collect(Collectors.toList());
		Collection<Pair> pairs = Pair.possiblePairs(indexes, indexes);
		Collection<PairingPath> possiblePaths = PairingPath.getPossiblePaths(pairs, new PairingState(indexCount), true);
		System.out.println(possiblePaths.stream()
				.map(Objects::toString)
				.collect(Collectors.joining("|")));
		indexes.forEach(i -> {
			Assert.assertTrue(possiblePaths.stream().anyMatch(
					path -> path.getPairs().stream().anyMatch(
							p -> p.getRow() == i || p.getColumn() == i)));
		});
		possiblePaths.forEach(path -> Assert.assertEquals(indexCount, path.getPairs().size()));
		Assert.assertEquals(expectedPathCount, possiblePaths.size());
	}

	@Test
	public void testPossiblePath_2() {
		testPossiblePath(2, 4);
	}

	@Test
	public void testPossiblePath_3() {
		testPossiblePath(3, 6);
	}

}
