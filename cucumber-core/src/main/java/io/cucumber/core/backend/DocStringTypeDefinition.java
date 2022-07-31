package io.cucumber.core.backend;

import io.cucumber.docstring.DocStringType;
import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL)
public interface DocStringTypeDefinition extends Located {

    DocStringType docStringType();

}
