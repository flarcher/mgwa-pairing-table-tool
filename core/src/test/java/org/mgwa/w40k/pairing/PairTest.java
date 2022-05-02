package org.mgwa.w40k.pairing;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PairTest {

    @Test
    public void testPairCount() {
        Assert.assertEquals(1, Pair.possiblePairsCount(1));
        Assert.assertEquals(4, Pair.possiblePairsCount(2));
        Assert.assertEquals(9, Pair.possiblePairsCount(3));
    }

    @Test
    public void Pairing_one() {
        List<Integer> rows = Arrays.asList(0);
        List<Integer> cols = Arrays.asList(3);
        Collection<Pair> pairs = Pair.possiblePairs(rows, cols);
        Assert.assertEquals(1, pairs.size());
        Pair singlePair = pairs.iterator().next();
        Assert.assertEquals(0, singlePair.getRow());
        Assert.assertEquals(3, singlePair.getColumn());
    }

    @Test
    public void Pairing_two() {
        List<Integer> rows = Arrays.asList(2, 4);
        List<Integer> cols = Arrays.asList(3, 5);
        Collection<Pair> pairs = Pair.possiblePairs(rows, cols);
        Assert.assertEquals(4, pairs.size());
        Assert.assertTrue(pairs.contains(Pair.of(2,3)));
        Assert.assertTrue(pairs.contains(Pair.of(2,5)));
        Assert.assertTrue(pairs.contains(Pair.of(4,3)));
        Assert.assertTrue(pairs.contains(Pair.of(4,5)));
    }

    @Test
    public void Pairing_three() {
        List<Integer> rows = Arrays.asList(2, 4, 6);
        List<Integer> cols = Arrays.asList(3, 5, 7);
        Collection<Pair> pairs = Pair.possiblePairs(rows, cols);
        //pairs.forEach(p -> System.out.println(p));
        Assert.assertEquals(9, pairs.size());
        Assert.assertTrue(pairs.contains(Pair.of(2,3)));
        Assert.assertTrue(pairs.contains(Pair.of(2,5)));
        Assert.assertTrue(pairs.contains(Pair.of(2,7)));
        Assert.assertTrue(pairs.contains(Pair.of(4,3)));
        Assert.assertTrue(pairs.contains(Pair.of(4,5)));
        Assert.assertTrue(pairs.contains(Pair.of(4,7)));
        Assert.assertTrue(pairs.contains(Pair.of(6,3)));
        Assert.assertTrue(pairs.contains(Pair.of(6,5)));
        Assert.assertTrue(pairs.contains(Pair.of(6,7)));
    }

}
