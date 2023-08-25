package org.mgwa.w40k.pairing.api.mapper;

import org.mgwa.w40k.pairing.ScoreReading;

public class ScoreReadingJsonDeserializer extends EnumJsonMapper.EnumJsonDeserializer<ScoreReading> {
    public ScoreReadingJsonDeserializer() {
        super(ScoreReading.MITIGATED);
    }
}
