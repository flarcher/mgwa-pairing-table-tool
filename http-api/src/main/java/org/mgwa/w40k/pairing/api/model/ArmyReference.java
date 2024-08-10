package org.mgwa.w40k.pairing.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mgwa.w40k.pairing.Army;

import java.util.Objects;
import java.util.Optional;

public class ArmyReference {

    public static ArmyReference from(Army army) {
        ArmyReference ar = new ArmyReference();
        ar.setIsRow(army.isRow());
        ar.setIndex(army.getIndex());
        ar.setName(army.getName());
        return ar;
    }

    @JsonProperty(value = "is_row", required = false)
    private Boolean isRow;

    @JsonProperty(value = "index", required = false)
    private Integer index;

    @JsonProperty(value = "name", required = false)
    private String name;

    public Optional<Boolean> isRow() {
        return Optional.ofNullable(isRow);
    }

    public Optional<Integer> getIndex() {
        return Optional.ofNullable(index);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public ArmyReference setIsRow(boolean row) {
        isRow = row;
        return this;
    }

    public ArmyReference setIndex(int index) {
        this.index = index;
        return this;
    }

    public ArmyReference setName(String name) {
        this.name = Objects.requireNonNull(name);
        return this;
    }

    public boolean isValid() {
        return name != null && !name.isEmpty()
            || (index != null && index >= 0 && isRow != null);
    }

    @Override
    public String toString() {
        return "{" +
                "isRow=" + isRow +
                ", index=" + index +
                ", name='" + name + '\'' +
                '}';
    }
}
