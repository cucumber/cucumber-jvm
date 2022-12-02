package io.cucumber.core.order.factory;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.order.PickleOrder;

import java.util.Comparator;

public final class LexicalUriPickleOrderFactory implements PickleOrderFactory {

    private static final Comparator<Pickle> pickleUriComparator = Comparator.comparing(Pickle::getUri)
            .thenComparing(pickle -> pickle.getLocation().getLine());
    private static final PickleOrder lexicalUriOrder = pickles -> {
        pickles.sort(pickleUriComparator);
        return pickles;
    };

    @Override
    public String getName() {
        return "normal";
    }

    @Override
    public PickleOrder create(String argument) {
        return lexicalUriOrder;
    }
}
