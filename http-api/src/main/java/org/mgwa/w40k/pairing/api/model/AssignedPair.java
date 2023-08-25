package org.mgwa.w40k.pairing.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class AssignedPair {

    @JsonProperty(value = "table_index", required = false)
    private Integer tableIndex;

    public Optional<Integer> getTableIndex() {
        return Optional.ofNullable(tableIndex);
    }

    @JsonProperty(value = "left_army", required = true)
    private ArmyReference leftArmy;

    @JsonProperty(value = "right_army", required = true)
    private ArmyReference rightArmy;

    public ArmyReference getLeftArmy() {
        return leftArmy;
    }

    public ArmyReference getRightArmy() {
        return rightArmy;
    }
}
