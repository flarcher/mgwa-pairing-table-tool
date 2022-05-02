package org.mgwa.w40k.pairing;

import org.junit.Assert;
import org.junit.Test;
import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.Score;
import static org.mgwa.w40k.pairing.PairingGuidance.ScoredPair;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Tests the class {@link PairingGuidance}.
 */
public class PairingGuidanceTest {

	private Matrix createMatrix_of1() {
		Matrix m = new Matrix(1);
		m.setScore(0, 0, Score.of(0, 20));
		Assert.assertTrue(m.isComplete());
		return m;
	}

	private Matrix createMatrix_of2() {
		Matrix m = new Matrix(2);
		m.setScore(0, 0, Score.of(0, 20));
		m.setScore(0, 1, Score.of(20, 20));
		m.setScore(1, 0, Score.of(0, 10));
		m.setScore(1, 1, Score.of(0, 5));
		Assert.assertTrue(m.isComplete());
		return m;
	}

	private Matrix createMatrix_of3() {
		Matrix m = new Matrix(3);
		m.setScore(0, 0, Score.of(0, 20));
		m.setScore(0, 1, Score.of(20, 20));
		m.setScore(0, 2, Score.of(10, 20));
		m.setScore(1, 0, Score.of(0, 10));
		m.setScore(1, 1, Score.of(0, 5));
		m.setScore(1, 2, Score.of(0, 0));
		m.setScore(2, 0, Score.of(5, 10));
		m.setScore(2, 1, Score.of(10, 10));
		m.setScore(2, 2, Score.of(10, 15));
		Assert.assertTrue(m.isComplete());
		return m;
	}

	private void checkScoredPairs(SortedSet<ScoredPair> scoredPairs, Predicate<Pair> predicate, int count) {
		// For debug
		System.out.println(scoredPairs.stream()
				.map(Object::toString)
				.collect(Collectors.joining(",")));
		AtomicReference<ScoredPair> previous = new AtomicReference<>();
		AtomicInteger counter = new AtomicInteger();
		scoredPairs.forEach(sp -> {
			Assert.assertTrue(predicate.test(sp.getPair()));
			Assert.assertTrue(sp.getScore() >= Score.MIN_VALUE);
			Assert.assertTrue(sp.getScore() <= Score.MAX_VALUE);
			ScoredPair psp = previous.get();
			if (psp != null) { // Check ordering
				Assert.assertTrue(psp.getScore() >= sp.getScore());
			}
			previous.set(sp);
			counter.incrementAndGet();
		});
		Assert.assertEquals(count, counter.get());
	}

	@Test
	public void testOf1_confident() {
		PairingGuidance pg = new PairingGuidance(createMatrix_of1(), System.out::println);
		Assignment assignment = Assignment.createEmpty(1);
		Predicate<Pair> predicate = p -> true;
		// Confident
		SortedSet<ScoredPair> scoredPairs = pg.suggestPairing(
				ScoreReading.CONFIDENT, assignment, predicate, ForecastMethod.LUCKY_BUT_RISKY);
		checkScoredPairs(scoredPairs, predicate, 1);
		Assert.assertEquals(1, scoredPairs.size());
		Assert.assertEquals(20, scoredPairs.iterator().next().getScore());
	}

	@Test
	public void testOf1_pessimistic() {
		PairingGuidance pg = new PairingGuidance(createMatrix_of1(), System.out::println);
		Assignment assignment = Assignment.createEmpty(1);
		Predicate<Pair> predicate = p -> true;
		// Confident
		SortedSet<ScoredPair> scoredPairs = pg.suggestPairing(
				ScoreReading.PESSIMISTIC, assignment, predicate, ForecastMethod.LUCKY_BUT_RISKY);
		checkScoredPairs(scoredPairs, predicate, 1);
		Assert.assertEquals(1, scoredPairs.size());
		Assert.assertEquals(0, scoredPairs.iterator().next().getScore());
	}

	@Test
	public void testOf1_mitigated() {
		PairingGuidance pg = new PairingGuidance(createMatrix_of1(), System.out::println);
		Assignment assignment = Assignment.createEmpty(1);
		Predicate<Pair> predicate = p -> true;
		// Confident
		SortedSet<ScoredPair> scoredPairs = pg.suggestPairing(
				ScoreReading.MITIGATED, assignment, predicate, ForecastMethod.LUCKY_BUT_RISKY);
		checkScoredPairs(scoredPairs, predicate, 1);
		Assert.assertEquals(1, scoredPairs.size());
		Assert.assertEquals(10, scoredPairs.iterator().next().getScore());
	}

	@Test
	public void testOf2_confident() {
		PairingGuidance pg = new PairingGuidance(createMatrix_of2(), System.out::println);
		Assignment assignment = Assignment.createEmpty(2);
		Predicate<Pair> predicate = Pair.isWithRow(0);
		for (ForecastMethod method : ForecastMethod.values()) {
			SortedSet<ScoredPair> scoredPairs = pg.suggestPairing(ScoreReading.CONFIDENT, assignment, predicate, method);
			checkScoredPairs(scoredPairs, predicate, 2);
			Iterator<ScoredPair> iterator = scoredPairs.iterator();
			ScoredPair topPair = iterator.next();
			Assert.assertEquals(15, topPair.getScore());
			Assert.assertEquals(Pair.of(0, 1), topPair.getPair());
			topPair = iterator.next();
			Assert.assertEquals(12, topPair.getScore());
			Assert.assertEquals(Pair.of(0, 0), topPair.getPair());
		}
	}

	@Test
	public void testOf2_mitigated() {
		PairingGuidance pg = new PairingGuidance(createMatrix_of2(), System.out::println);
		Assignment assignment = Assignment.createEmpty(2);
		Predicate<Pair> predicate = Pair.isWithRow(0);
		for (ForecastMethod method : ForecastMethod.values()) {
			SortedSet<ScoredPair> scoredPairs = pg.suggestPairing(ScoreReading.MITIGATED, assignment, predicate, method);
			checkScoredPairs(scoredPairs, predicate, 2);
			Iterator<ScoredPair> iterator = scoredPairs.iterator();
			ScoredPair topPair = iterator.next();
			Assert.assertEquals(12, topPair.getScore());
			Assert.assertEquals(Pair.of(0, 1), topPair.getPair());
			topPair = iterator.next();
			Assert.assertEquals(6, topPair.getScore());
			Assert.assertEquals(Pair.of(0, 0), topPair.getPair());
		}
	}

	@Test
	public void testOf2_pessimistic() {
		PairingGuidance pg = new PairingGuidance(createMatrix_of2(), System.out::println);
		Assignment assignment = Assignment.createEmpty(2);
		Predicate<Pair> predicate = Pair.isWithRow(0);
		for (ForecastMethod method : ForecastMethod.values()) {
			SortedSet<ScoredPair> scoredPairs = pg.suggestPairing(ScoreReading.PESSIMISTIC, assignment, predicate, method);
			checkScoredPairs(scoredPairs, predicate, 2);
			Iterator<ScoredPair> iterator = scoredPairs.iterator();
			ScoredPair topPair = iterator.next();
			Assert.assertEquals(10, topPair.getScore());
			Assert.assertEquals(Pair.of(0, 1), topPair.getPair());
			topPair = iterator.next();
			Assert.assertEquals(0, topPair.getScore());
			Assert.assertEquals(Pair.of(0, 0), topPair.getPair());
		}
	}

	/**
	 *  With the method {@link ForecastMethod#LUCKY_BUT_RISKY}
	 */
	@Test
	public void testOf3_confident_luckyForecast() {
		PairingGuidance pg = new PairingGuidance(createMatrix_of3(), System.out::println);
		Assignment assignment = Assignment.createEmpty(3);
		Predicate<Pair> predicate = Pair.isWithRow(0);
		SortedSet<ScoredPair> scoredPairs = pg.suggestPairing(
			ScoreReading.CONFIDENT, assignment, predicate, ForecastMethod.LUCKY_BUT_RISKY);
		checkScoredPairs(scoredPairs, predicate, 3);
		Iterator<ScoredPair> iterator = scoredPairs.iterator();
		ScoredPair topPair = iterator.next();
		Assert.assertEquals(15, topPair.getScore());
		Assert.assertEquals(Pair.of(0, 1), topPair.getPair());
		topPair = iterator.next();
		Assert.assertEquals(13, topPair.getScore());
		Assert.assertEquals(Pair.of(0, 0), topPair.getPair());
		topPair = iterator.next();
		Assert.assertEquals(13, topPair.getScore());
		Assert.assertEquals(Pair.of(0, 2), topPair.getPair());
	}

	/**
	 *  With the method {@link ForecastMethod#AVERAGE}
	 */
	@Test
	public void testOf3_confident_averageForecast() {
		PairingGuidance pg = new PairingGuidance(createMatrix_of3(), System.out::println);
		Assignment assignment = Assignment.createEmpty(3);
		Predicate<Pair> predicate = Pair.isWithRow(0);
		SortedSet<ScoredPair> scoredPairs = pg.suggestPairing(
				ScoreReading.CONFIDENT, assignment, predicate, ForecastMethod.AVERAGE);
		checkScoredPairs(scoredPairs, predicate, 3);
		Iterator<ScoredPair> iterator = scoredPairs.iterator();
		ScoredPair topPair = iterator.next();
		Assert.assertEquals(16, topPair.getScore());
		Assert.assertEquals(Pair.of(0, 2), topPair.getPair());
		topPair = iterator.next();
		Assert.assertEquals(15, topPair.getScore());
		Assert.assertEquals(Pair.of(0, 0), topPair.getPair());
		topPair = iterator.next();
		Assert.assertEquals(15, topPair.getScore());
		Assert.assertEquals(Pair.of(0, 1), topPair.getPair());
	}
}
