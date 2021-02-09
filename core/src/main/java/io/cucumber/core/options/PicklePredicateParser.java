package io.cucumber.core.options;

import io.cucumber.core.gherkin.Pickle;

import java.util.function.Predicate;

public final class PicklePredicateParser {

    @SuppressWarnings("unchecked")
    public static Class<? extends Predicate<Pickle>> parsePicklePredicateClass(String predicateClassName) {
        Class<?> clazz;
        try {
            clazz = Class.forName(predicateClassName);
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IllegalArgumentException(
                String.format("Could not load predicate class for '%s'", predicateClassName), e);
        }
        if (!Predicate.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(String.format("Predicate class '%s' was not a subclass of '%s'",
                clazz, Predicate.class));
        }
        return (Class<? extends Predicate<Pickle>>) clazz;
    }

}
