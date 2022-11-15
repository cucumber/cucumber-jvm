package io.cucumber.core.order;

import io.cucumber.core.gherkin.Pickle;

import java.util.List;

public interface PickleOrder {

    void setArgument(String argument);

    String getName();

    List<Pickle> orderPickles(List<Pickle> pickles);

}
