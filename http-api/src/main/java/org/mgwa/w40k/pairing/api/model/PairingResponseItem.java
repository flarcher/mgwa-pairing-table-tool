package org.mgwa.w40k.pairing.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;

@Immutable
public class PairingResponseItem {

    private final static String ATTR_COLUMN_ARMY = "col_army";
    private final static String ATTR_ROW_ARMY = "row_army";
    private final static String ATTR_SCORE = "score";

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public PairingResponseItem(
            @JsonProperty(value = ATTR_SCORE, required = true) int score,
            @JsonProperty(value = ATTR_ROW_ARMY, required = true) ArmyReference leftArmy,
            @JsonProperty(value = ATTR_COLUMN_ARMY, required = true) ArmyReference rightArmy) {
        this.score = score;
        this.leftArmy = leftArmy;
        this.rightArmy = rightArmy;
    }

    @JsonProperty(value = ATTR_SCORE, required = true)
    private final int score;

    @JsonProperty(value = ATTR_ROW_ARMY, required = true)
    private final ArmyReference leftArmy;

    @JsonProperty(value = ATTR_COLUMN_ARMY, required = true)
    private final ArmyReference rightArmy;

    @JsonIgnore
    public int getScore() {
        return score;
    }

    @JsonIgnore
    public ArmyReference getLeftArmy() {
        return leftArmy;
    }

    @JsonIgnore
    public ArmyReference getRightArmy() {
        return rightArmy;
    }
}
