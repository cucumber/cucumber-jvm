package io.cucumber.java8;

import org.apiguardian.api.API;

import java.util.Map;

@FunctionalInterface
@API(status = API.Status.STABLE)
public interface DataTableEntryDefinitionBody<T> {

    T accept(Map<String, String> entry) throws Throwable;

}
