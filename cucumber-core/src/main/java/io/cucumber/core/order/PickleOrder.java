package io.cucumber.core.order;

import io.cucumber.core.gherkin.Pickle;
import org.apiguardian.api.API;

import java.util.List;

@API(status = API.Status.EXPERIMENTAL)
public interface PickleOrder {

    List<Pickle> orderPickles(List<Pickle> pickles);

}
