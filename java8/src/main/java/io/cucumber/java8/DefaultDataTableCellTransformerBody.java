package io.cucumber.java8;

import org.apiguardian.api.API;

import java.lang.reflect.Type;

@FunctionalInterface
@API(status = API.Status.STABLE)
public interface DefaultDataTableCellTransformerBody {

    Object accept(String fromValue, Type toValueType) throws Throwable;

}
