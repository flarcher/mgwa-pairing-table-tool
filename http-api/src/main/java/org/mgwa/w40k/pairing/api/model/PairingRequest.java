package org.mgwa.w40k.pairing.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.mgwa.w40k.pairing.ForecastMethod;
import org.mgwa.w40k.pairing.ScoreReading;
import org.mgwa.w40k.pairing.api.mapper.*;

import javax.annotation.concurrent.Immutable;
import java.util.List;

@Immutable
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
    private ArmyReference nextArmy;

    @JsonProperty("assigned_pairs")
    private List<AssignedPair> assignedPairs;

    public ForecastMethod getMethod() {
        return method;
    }

    public ScoreReading getReading() {
        return reading;
    }

    public ArmyReference getNextArmy() {
        return nextArmy;
    }

    public List<AssignedPair> getAssignedPairs() {
        return assignedPairs;
    }
}
