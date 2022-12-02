package io.cucumber.core.order.factory;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.order.PickleOrder;

import java.util.Comparator;

public final class ReverseLexicalUriPickleOrderFactory implements PickleOrderFactory {

    private static final Comparator<Pickle> pickleUriReversedComparator = Comparator.comparing(Pickle::getUri)
            .thenComparing(pickle -> pickle.getLocation().getLine()).reversed();

    private static final PickleOrder reverseLexicalUriOrder = pickles -> {
        pickles.sort(pickleUriReversedComparator);
        return pickles;
    };

    @Override
    public String getName() {
        return "reverse";
    }

    @Override
    public PickleOrder create(String argument) {
        return reverseLexicalUriOrder;
    }
}
