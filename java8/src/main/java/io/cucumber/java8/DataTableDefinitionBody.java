package io.cucumber.java8;

import io.cucumber.datatable.DataTable;
import org.apiguardian.api.API;

@FunctionalInterface
@API(status = API.Status.STABLE)
public interface DataTableDefinitionBody<T> {

    T accept(DataTable table) throws Throwable;

}
