package io.cucumber.java8;

import org.apiguardian.api.API;

import java.util.List;

@FunctionalInterface
@API(status = API.Status.STABLE)
public interface DataTableRowDefinitionBody<T> {

    T accept(List<String> row) throws Throwable;

}
