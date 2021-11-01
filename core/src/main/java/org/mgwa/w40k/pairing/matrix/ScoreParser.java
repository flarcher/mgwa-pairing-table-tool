package org.mgwa.w40k.pairing.matrix;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transform a matrix value into a {@link Score}.
 */
public class ScoreParser implements Function<String, Score> {

    private static final Pattern PATTERN = Pattern.compile("^(\\d+)[-_ ](\\d+)$");
    private static final Map<String, Supplier<Score>> SPECIAL_SCORES = Map.of(
        "0", () -> new Score(0, 4),
        "5", () -> new Score(5, 8),
        "10", () -> new Score(9, 11),
        "15", () -> new Score(12, 15),
        "20", () -> new Score(16, 20),
        "G", Score::newDefault,
        "-", () -> new Score(Score.MEDIUM_SCORE_VALUE, Score.MEDIUM_SCORE_VALUE));

    public ScoreParser() {}

    public Score applyOrFail(String input) throws IllegalArgumentException {
        Score score = apply(input);
        if (score == null) {
            throw new IllegalArgumentException(String.format("Invalid score %s", input));
        }
        return score;
    }

    @Override
    public Score apply(String input) {
        String s = input.trim();
        if (s.isEmpty()) {
            return Score.newDefault();
        }
        Supplier<Score> specialScore = SPECIAL_SCORES.get(s);
        if (specialScore != null) {
            return specialScore.get();
        }
        Matcher matcher = PATTERN.matcher(s);
        if (matcher.matches()) {
            String minStr = matcher.group(1);
            String maxStr = matcher.group(2);
            try {
                return new Score(
                    Integer.parseInt(minStr),
                    Integer.parseInt(maxStr));
            }
            catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
