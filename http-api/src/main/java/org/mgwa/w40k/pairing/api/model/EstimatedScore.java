package org.mgwa.w40k.pairing.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mgwa.w40k.pairing.matrix.Score;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Immutable
public class EstimatedScore {

    public static EstimatedScore from(Score score) {
        return new EstimatedScore(
            score.getMinValue(),
            score.getMaxValue());
    }

    private EstimatedScore(int minimum, int maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @JsonProperty("min")
    @Min(0L)
    @Max(20L)
    private final int minimum;
    @JsonProperty("max")
    @Min(0L)
    @Max(20L)
    private final int maximum;

    public int getMinimum() {
        return minimum;
    }

    public int getMaximum() {
        return maximum;
    }
}
