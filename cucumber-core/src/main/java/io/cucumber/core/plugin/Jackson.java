package io.cucumber.core.plugin;

import io.cucumber.messages.ndjson.internal.com.fasterxml.jackson.annotation.JsonCreator.Mode;
import io.cucumber.messages.ndjson.internal.com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.cucumber.messages.ndjson.internal.com.fasterxml.jackson.core.JsonGenerator;
import io.cucumber.messages.ndjson.internal.com.fasterxml.jackson.databind.DeserializationFeature;
import io.cucumber.messages.ndjson.internal.com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.messages.ndjson.internal.com.fasterxml.jackson.databind.SerializationFeature;
import io.cucumber.messages.ndjson.internal.com.fasterxml.jackson.databind.cfg.ConstructorDetector;
import io.cucumber.messages.ndjson.internal.com.fasterxml.jackson.databind.json.JsonMapper;
import io.cucumber.messages.ndjson.internal.com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.cucumber.messages.ndjson.internal.com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import static io.cucumber.messages.ndjson.internal.com.fasterxml.jackson.annotation.JsonInclude.Value.construct;

final class Jackson {
    public static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .addModule(new Jdk8Module())
            .addModule(new ParameterNamesModule(Mode.PROPERTIES))
            .defaultPropertyInclusion(construct(
                Include.NON_ABSENT,
                Include.NON_ABSENT))
            .constructorDetector(ConstructorDetector.USE_PROPERTIES_BASED)
            .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
            .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
            .enable(DeserializationFeature.USE_LONG_FOR_INTS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
            .build();

    private Jackson() {
    }

}
