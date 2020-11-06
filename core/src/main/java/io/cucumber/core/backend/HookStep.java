package io.cucumber.core.backend;

import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL)
public interface HookStep extends Step {

    PickleStep getRelatedStep();

}
