package org.mgwa.w40k.pairing.api.mapper;

import org.mgwa.w40k.pairing.ForecastMethod;

public class ForecastMethodJsonDeserializer extends EnumJsonMapper.EnumJsonDeserializer<ForecastMethod> {
    public ForecastMethodJsonDeserializer() {
        super(ForecastMethod.AVERAGE);
    }
}
