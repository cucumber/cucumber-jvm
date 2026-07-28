package io.cucumber.core.resource;

import java.util.Objects;
import java.util.function.Predicate;

public final class ClassFilter {

    private final Predicate<String> namePredicate;
    private final Predicate<Class<?>> classPredicate;

    private ClassFilter(Predicate<String> namePredicate, Predicate<Class<?>> classPredicate) {
        this.namePredicate = Objects.requireNonNull(namePredicate);
        this.classPredicate = Objects.requireNonNull(classPredicate);
    }

    public static ClassFilter of(Predicate<String> namePredicate, Predicate<Class<?>> classPredicate) {
        return new ClassFilter(namePredicate, classPredicate);
    }

    public static ClassFilter of(Predicate<Class<?>> classPredicate) {
        return new ClassFilter(className -> true, classPredicate);
    }

    boolean match(String name) {
        return namePredicate.test(name);
    }

    boolean match(Class<?> type) {
        return classPredicate.test(type);
    }

}
