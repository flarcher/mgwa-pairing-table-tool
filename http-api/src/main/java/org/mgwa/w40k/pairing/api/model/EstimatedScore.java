package org.mgwa.w40k.pairing.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.mgwa.w40k.pairing.matrix.Score;

import javax.annotation.concurrent.Immutable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Immutable
public class EstimatedScore {

    public static EstimatedScore from(Score score) {
        return new EstimatedScore(
            score.getMinValue(),
            score.getMaxValue());
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public EstimatedScore(
            @JsonProperty("min") @Min(0L) @Max(20L) int minimum,
            @JsonProperty("max") @Min(0L) @Max(20L) int maximum) {
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

    public Score toScore() {
        return Score.of(minimum, maximum);
    }
}
