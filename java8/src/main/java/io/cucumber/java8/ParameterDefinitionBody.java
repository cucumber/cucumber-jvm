package io.cucumber.java8;


public interface ParameterDefinitionBody<R> {
    @FunctionalInterface
    interface A1<R, T1> extends ParameterDefinitionBody<R> {
        R accept(T1 p1) throws Throwable;
    }
    @FunctionalInterface
    interface A2<R, T1, T2> extends ParameterDefinitionBody<R> {
        R accept(T1 p1, T2 p2) throws Throwable;
    }
    @FunctionalInterface
    interface A3<R, T1, T2, T3> extends ParameterDefinitionBody<R> {
        R accept(T1 p1, T2 p2, T3 p3) throws Throwable;
    }
}
