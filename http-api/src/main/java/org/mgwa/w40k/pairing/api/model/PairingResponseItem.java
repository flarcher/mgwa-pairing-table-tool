package org.mgwa.w40k.pairing.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;

@Immutable
public class PairingResponseItem {

    public PairingResponseItem(int score, ArmyReference leftArmy, ArmyReference rightArmy) {
        this.score = score;
        this.leftArmy = leftArmy;
        this.rightArmy = rightArmy;
    }

    @JsonProperty(value = "score", required = true)
    private int score;

    @JsonProperty(value = "row_army", required = true)
    private ArmyReference leftArmy;

    @JsonProperty(value = "column_army", required = true)
    private ArmyReference rightArmy;

    public int getScore() {
        return score;
    }

    public ArmyReference getLeftArmy() {
        return leftArmy;
    }

    public ArmyReference getRightArmy() {
        return rightArmy;
    }
}
