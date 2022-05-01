package org.mgwa.w40k.pairing;

import org.junit.Assert;
import org.junit.Test;
import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.Score;
import static org.mgwa.w40k.pairing.PairingGuidance.ScoredPair;

import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Tests the class {@link PairingGuidance}.
 */
public class PairingGuidanceTest {

	private static final int TABLE_COUNT = 3;

	private Matrix createMatrix() {
		Matrix m = new Matrix(TABLE_COUNT);
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

	private void checkScoredPairs(SortedSet<ScoredPair> scoredPairs, Predicate<Pair> predicate) {
		// For debug
		System.out.println(scoredPairs.stream()
				.map(Object::toString)
				.collect(Collectors.joining(",")));
		AtomicReference<ScoredPair> previous = new AtomicReference<>();
		scoredPairs.forEach(sp -> {
			Assert.assertTrue(predicate.test(sp.getPair()));
			Assert.assertTrue(sp.getScore() >= Score.MIN_VALUE);
			Assert.assertTrue(sp.getScore() <= Score.MAX_VALUE);
			ScoredPair psp = previous.get();
			if (psp != null) { // Check ordering
				Assert.assertTrue(psp.getScore() <= sp.getScore());
			}
			previous.set(sp);
		});
	}

	/**
	 *  With the method {@link ForecastMethod#LUCKY_BUT_RISKY}
	 */
	@Test
	public void test_luckyForecast_confident() {
		PairingGuidance pg = new PairingGuidance(createMatrix(), System.out::println);
		Assignment assignment = Assignment.createEmpty(TABLE_COUNT);
		Predicate<Pair> predicate = Pair.isWithRow(0);
		SortedSet<ScoredPair> scoredPairs = pg.suggestPairing(
			ScoreReading.CONFIDENT, assignment, predicate, ForecastMethod.LUCKY_BUT_RISKY);
		checkScoredPairs(scoredPairs, predicate);
		// TODO
	}

	/**
	 *  With the method {@link ForecastMethod#AVERAGE}
	 */
	// TODO
}
