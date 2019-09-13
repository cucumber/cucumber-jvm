package io.cucumber.core.backend;

import io.cucumber.datatable.TableCellByTypeTransformer;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface DefaultDataTableCellTransformerDefinition extends Located {

    TableCellByTypeTransformer tableCellByTypeTransformer();

}
