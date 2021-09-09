package io.cucumber.java8;

import org.apiguardian.api.API;

@FunctionalInterface
@API(status = API.Status.STABLE)
public interface DataTableCellDefinitionBody<T> {

    T accept(String cell) throws Throwable;

}
