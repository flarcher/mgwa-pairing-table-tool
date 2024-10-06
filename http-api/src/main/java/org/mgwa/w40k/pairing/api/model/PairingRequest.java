package org.mgwa.w40k.pairing.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.mgwa.w40k.pairing.ForecastMethod;
import org.mgwa.w40k.pairing.ScoreReading;
import org.mgwa.w40k.pairing.api.mapper.*;

import java.util.List;
import java.util.Optional;

/**
 * A request for a pairing of 'attackers' with a 'defender' army.
 */
public class PairingRequest {

    @JsonProperty("method")
    @JsonDeserialize(using = ForecastMethodJsonDeserializer.class)
    @JsonSerialize(using = ForecastMethodJsonSerializer.class)
    private ForecastMethod method;

    @JsonProperty("reading")
    @JsonDeserialize(using = ScoreReadingJsonDeserializer.class)
    @JsonSerialize(using = ScoreReadingJsonSerializer.class)
    private ScoreReading reading;

    @JsonProperty("next_army")
    private Optional<ArmyReference> nextArmy;

    @JsonProperty("assigned_pairs")
    private List<AssignedPair> assignedPairs;

    @JsonIgnore
    public ForecastMethod getMethod() {
        return method;
    }

    @JsonIgnore
    public ScoreReading getReading() {
        return reading;
    }

    @JsonIgnore
    public Optional<ArmyReference> getNextArmy() {
        return nextArmy;
    }

    @JsonIgnore
    public List<AssignedPair> getAssignedPairs() {
        return assignedPairs;
    }

    @JsonIgnore
    public void setMethod(ForecastMethod method) {
        this.method = method;
    }

    @JsonIgnore
    public void setReading(ScoreReading reading) {
        this.reading = reading;
    }

    @JsonIgnore
    public void setNextArmy(Optional<ArmyReference> nextArmy) {
        this.nextArmy = nextArmy;
    }

    @JsonIgnore
    public void setAssignedPairs(List<AssignedPair> assignedPairs) {
        this.assignedPairs = assignedPairs;
    }
}
