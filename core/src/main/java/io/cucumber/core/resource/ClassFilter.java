package io.cucumber.core.resource;

import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;


public final class ClassFilter implements Predicate<Class<?>> {

    private final Predicate<String> namePredicate;
    private final Predicate<Class<?>> classPredicate;

    private ClassFilter(Predicate<String> namePredicate, Predicate<Class<?>> classPredicate) {
        this.namePredicate = requireNonNull(namePredicate, "name predicate must not be null");
        this.classPredicate = requireNonNull(classPredicate, "class predicate must not be null");
    }

    public static ClassFilter of(Predicate<Class<?>> classPredicate) {
        return of(name -> true, classPredicate);
    }

    public static ClassFilter of(Predicate<String> namePredicate, Predicate<Class<?>> classPredicate) {
        return new ClassFilter(namePredicate, classPredicate);
    }

    public boolean match(String name) {
        return namePredicate.test(name);
    }

    public boolean match(Class<?> type) {
        return classPredicate.test(type);
    }

    @Override
    public boolean test(Class<?> type) {
        return match(type.getName()) && match(type);
    }
}
