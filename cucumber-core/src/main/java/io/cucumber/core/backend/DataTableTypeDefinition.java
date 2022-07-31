package io.cucumber.core.backend;

import io.cucumber.datatable.DataTableType;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface DataTableTypeDefinition extends Located {

    DataTableType dataTableType();

}
