package io.cucumber.core.order;

import io.cucumber.core.gherkin.Pickle;

import java.util.List;

public interface PickleOrder {

    List<Pickle> orderPickles(List<Pickle> pickles);

}
