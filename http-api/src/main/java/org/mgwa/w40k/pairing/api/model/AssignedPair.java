package org.mgwa.w40k.pairing.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class AssignedPair {

    @JsonProperty(value = "table_index", required = false)
    private Integer tableIndex;

    @JsonIgnore
    public Optional<Integer> getTableIndex() {
        return Optional.ofNullable(tableIndex);
    }

    @JsonProperty(value = "left_army", required = true)
    private ArmyReference leftArmy;

    @JsonProperty(value = "right_army", required = true)
    private ArmyReference rightArmy;

    @JsonIgnore
    public ArmyReference getLeftArmy() {
        return leftArmy;
    }

    @JsonIgnore
    public ArmyReference getRightArmy() {
        return rightArmy;
    }

    @JsonIgnore
    public void setTableIndex(Integer tableIndex) {
        this.tableIndex = tableIndex;
    }

    @JsonIgnore
    public void setLeftArmy(ArmyReference leftArmy) {
        this.leftArmy = leftArmy;
    }

    @JsonIgnore
    public void setRightArmy(ArmyReference rightArmy) {
        this.rightArmy = rightArmy;
    }
}
