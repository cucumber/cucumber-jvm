package io.cucumber.core.order;

import io.cucumber.core.feature.CucumberPickle;

import java.util.List;

public interface PickleOrder {

	List<CucumberPickle> orderPickles(List<CucumberPickle> pickleEvents);
}
