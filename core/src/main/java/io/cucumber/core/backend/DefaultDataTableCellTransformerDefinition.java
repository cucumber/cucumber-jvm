package io.cucumber.core.backend;

import io.cucumber.datatable.TableCellByTypeTransformer;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface DefaultDataTableCellTransformerDefinition {

    TableCellByTypeTransformer tableCellByTypeTransformer();

    /**
     * The source line where the default data table cell  is defined.
     * Example: com/example/app/Cucumber.test():42
     *
     * @return The source line of the step definition.
     */
    String getLocation();

}
