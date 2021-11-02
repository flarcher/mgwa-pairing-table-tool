package org.mgwa.w40k.pairing.matrix;

import org.junit.Assert;
import org.junit.Test;

public class ScoreTest {

    private final int MIN = 2;
    private final int MAX = 12;
    private static final float DELTA = 0.1f;

    @Test
    public void assertNominalValues() {
        Assert.assertTrue(MIN >= Score.MIN_VALUE);
        Assert.assertTrue(MIN < MAX);
        Assert.assertTrue(MAX <= Score.MAX_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void outOfBoundsMinimum() {
        int minimum = -1;
        Assert.assertTrue(minimum < Score.MIN_VALUE);
        Score.of(minimum, MAX);
    }

    @Test(expected = IllegalArgumentException.class)
    public void outOfBoundsMaximum() {
        int maximum = 42;
        Assert.assertTrue(maximum > Score.MAX_VALUE);
        Score.of(MIN, maximum);
    }

    @Test
    public void updateBiggerMin() {
        Score score = Score.of(1, 12);
        int newMinimum = 5;
        Assert.assertTrue(score.getMinValue() < newMinimum);
        Assert.assertTrue(newMinimum < 12);
        score.updateMinValue(newMinimum);
        Assert.assertEquals(newMinimum, score.getMinValue());
        Assert.assertEquals(12, score.getMaxValue());
        Assert.assertEquals(1, score.getCount());
        Assert.assertEquals(8.5, score.getAverage(), DELTA);
    }

    @Test
    public void updateLowerMin() {
        Score score = Score.of(5, 12);
        int newMinimum = 2;
        Assert.assertTrue(newMinimum < score.getMinValue());
        Assert.assertTrue(newMinimum < 12);
        score.updateMinValue(newMinimum);
        Assert.assertEquals(newMinimum, score.getMinValue());
        Assert.assertEquals(12, score.getMaxValue());
        Assert.assertEquals(1, score.getCount());
        Assert.assertEquals(7.0, score.getAverage(), DELTA);
    }

    @Test
    public void updateTooBigMin() {
        Score score = Score.of(5, 12);
        int newMinimum = 13;
        Assert.assertTrue(newMinimum > score.getMaxValue());
        Assert.assertTrue(newMinimum < Score.MAX_VALUE);
        score.updateMinValue(newMinimum);
        Assert.assertEquals(5, score.getMinValue());
        Assert.assertEquals(newMinimum, score.getMaxValue());
        Assert.assertEquals(1, score.getCount());
        Assert.assertEquals(9.0, score.getAverage(), DELTA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateOutOfBoundsMin() {
        Score score = Score.of(5, 12);
        int newMinimum = -2;
        Assert.assertTrue(newMinimum < Score.MIN_VALUE);
        score.updateMinValue(newMinimum);
    }

    @Test
    public void updateBiggerMax(){
        Score score = Score.of(5, 12);
        int newMaximum = 13;
        Assert.assertTrue(newMaximum > score.getMaxValue());
        Assert.assertTrue(newMaximum < Score.MAX_VALUE);
        score.updateMaxValue(newMaximum);
        Assert.assertEquals(5, score.getMinValue());
        Assert.assertEquals(newMaximum, score.getMaxValue());
        Assert.assertEquals(1, score.getCount());
        Assert.assertEquals(9.0, score.getAverage(), DELTA);
    }

    @Test
    public void updateLowerMax(){
        Score score = Score.of(5, 12);
        int newMaximum = 7;
        Assert.assertTrue(newMaximum > score.getMinValue());
        Assert.assertTrue(newMaximum < score.getMaxValue());
        score.updateMaxValue(newMaximum);
        Assert.assertEquals(5, score.getMinValue());
        Assert.assertEquals(newMaximum, score.getMaxValue());
        Assert.assertEquals(1, score.getCount());
        Assert.assertEquals(6.0, score.getAverage(), DELTA);
    }

    @Test
    public void updateTooLowMax() {
        Score score = Score.of(5, 12);
        int newMaximum = 4;
        Assert.assertTrue(newMaximum < score.getMinValue());
        Assert.assertTrue(newMaximum > Score.MIN_VALUE);
        score.updateMaxValue(newMaximum);
        Assert.assertEquals(newMaximum, score.getMinValue());
        Assert.assertEquals(12, score.getMaxValue());
        Assert.assertEquals(1, score.getCount());
        Assert.assertEquals(8.0, score.getAverage(), DELTA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateOutOfBoundsMax() {
        Score score = Score.of(5, 12);
        int newMaximum = 21;
        Assert.assertTrue(newMaximum > Score.MAX_VALUE);
        score.updateMaxValue(newMaximum);
    }
}
