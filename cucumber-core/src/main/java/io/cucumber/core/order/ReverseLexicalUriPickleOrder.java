package io.cucumber.core.order;

import io.cucumber.core.gherkin.Pickle;

import java.util.List;

import static io.cucumber.core.order.StandardPickleOrders.pickleUriComparator;

public class ReverseLexicalUriPickleOrder implements PickleOrder {
    @Override
    public void setArgument(String argument) {
    }

    @Override
    public String getName() {
        return "reverse";
    }

    @Override
    public List<Pickle> orderPickles(List<Pickle> pickles) {
        pickles.sort(pickleUriComparator.reversed());
        return pickles;
    }
}
