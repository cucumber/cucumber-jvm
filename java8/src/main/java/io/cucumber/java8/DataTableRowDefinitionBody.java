package io.cucumber.java8;

import java.util.List;
import org.apiguardian.api.API;

@FunctionalInterface
@API(status = API.Status.STABLE)
public interface DataTableRowDefinitionBody<T> {
    T accept(List<String> row) throws Throwable;
}
