package org.mgwa.w40k.pairing.matrix;

import org.junit.Assert;
import org.junit.Test;

public class ScoreParserTest {

    private final ScoreParser parser = new ScoreParser();

    private void assertSingleCountScore(Score score) {
        Assert.assertEquals(1, score.getCount());
        Assert.assertEquals(((float) (score.getMaxValue() + score.getMinValue())) / 2, score.getAverage(), 0.1);
    }

    private void assertDefaultScore(Score score) {
        Assert.assertEquals(Score.MIN_VALUE, score.getMinValue());
        Assert.assertEquals(Score.MAX_VALUE, score.getMaxValue());
        assertSingleCountScore(score);
    }

    @Test
    public void parseEmptyScore() {
        assertDefaultScore(parser.applyOrFail(""));
    }

    @Test
    public void parseGambleScore() {
        assertDefaultScore(parser.applyOrFail("G"));
    }

    @Test
    public void parseEqualScore() {
        Score score = parser.applyOrFail("-");
        Assert.assertEquals(Score.MEDIUM_SCORE_VALUE, score.getMinValue());
        Assert.assertEquals(Score.MEDIUM_SCORE_VALUE, score.getMaxValue());
        assertSingleCountScore(score);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseInvalidScore() {
        parser.applyOrFail("Invalid");
    }

    @Test
    public void parseSpecialValue() {
        Score score = parser.applyOrFail("10");
        Assert.assertEquals(9, score.getMinValue());
        Assert.assertEquals(11, score.getMaxValue());
        assertSingleCountScore(score);
    }

    @Test
    public void parseScoreInterval() {
        Score score = parser.applyOrFail("3-12");
        Assert.assertEquals(3, score.getMinValue());
        Assert.assertEquals(12, score.getMaxValue());
        assertSingleCountScore(score);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseOutOfBoundScoreInterval() {
        parser.applyOrFail("3-24");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseWrongScoreInterval() {
        parser.applyOrFail("A-14");
    }

    @Test
    public void testReverse() {
        Score initialScore = Score.of(0, 4);
        String scoreAsString = ScoreParser.reverse().apply(initialScore);
        Assert.assertEquals(initialScore, parser.apply(scoreAsString));
    }
}
