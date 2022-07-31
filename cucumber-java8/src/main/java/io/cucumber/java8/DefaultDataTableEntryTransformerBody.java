package io.cucumber.java8;

import org.apiguardian.api.API;

import java.lang.reflect.Type;
import java.util.Map;

@FunctionalInterface
@API(status = API.Status.STABLE)
public interface DefaultDataTableEntryTransformerBody {

    Object accept(Map<String, String> fromValue, Type toValueType) throws Throwable;

}
