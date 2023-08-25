package org.mgwa.w40k.pairing.api.mapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class EnumJsonMapper {

    private EnumJsonMapper() {} // Utility class

    private static <E extends Enum<E>> String enumToString(E enumInstance) {
        // Warning: it does not manage backward compatibility
        return enumInstance.name().trim().toLowerCase();
    }

    static class EnumJsonDeserializer<E extends Enum<E>> extends JsonDeserializer<E> {

        private final Map<String, E> enumMap;
        private final @Nullable E defaultValue;

        public EnumJsonDeserializer(Class<E> enumClass) {
            this(enumClass, null);
        }

        public EnumJsonDeserializer(@Nonnull E defaultValue) {
            this((Class<E>) defaultValue.getClass(), defaultValue);
        }

        private EnumJsonDeserializer(Class<E> enumClass, @Nullable E defaultValue) {
            this.defaultValue = defaultValue;
            E[] constants = enumClass.getEnumConstants();
            enumMap = Stream.of(constants)
                .collect(Collectors.toUnmodifiableMap(EnumJsonMapper::<E> enumToString, Function. <E>identity()));
        }

        @Override
        public E deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            ObjectCodec objectCodec = jsonParser.getCodec();
            JsonNode node = objectCodec.readTree(jsonParser);
            return enumMap.getOrDefault(node.asText(), this.defaultValue);
        }
    }

    static class EnumJsonSerializer<E extends Enum<E>> extends JsonSerializer<E> {

        @Override
        public void serialize(E e, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(EnumJsonMapper.enumToString(e));
        }
    }

}
