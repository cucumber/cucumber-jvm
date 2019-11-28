package io.cucumber.core.order;

import io.cucumber.core.gherkin.CucumberPickle;

import java.util.List;

public interface PickleOrder {

	List<? extends CucumberPickle> orderPickles(List<? extends CucumberPickle> pickles);
}
