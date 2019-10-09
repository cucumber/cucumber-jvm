package io.cucumber.java8;

import java.util.Map;
import org.apiguardian.api.API;

@FunctionalInterface
@API(status = API.Status.STABLE)
public interface DataTableEntryDefinitionBody<T> {
    T accept(Map<String, String> map) throws Throwable;
}
