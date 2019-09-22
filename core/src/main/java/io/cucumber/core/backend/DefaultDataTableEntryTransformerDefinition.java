package io.cucumber.core.backend;

import io.cucumber.datatable.TableEntryByTypeTransformer;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface DefaultDataTableEntryTransformerDefinition extends Located {

    boolean headersToProperties();

    TableEntryByTypeTransformer tableEntryByTypeTransformer();

}
