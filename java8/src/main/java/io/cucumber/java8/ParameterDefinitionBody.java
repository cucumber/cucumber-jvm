package io.cucumber.java8;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface ParameterDefinitionBody<R> {
    @FunctionalInterface
    interface A1<R> extends ParameterDefinitionBody<R> {
        R accept(String p1) throws Throwable;
    }
    @FunctionalInterface
    interface A2<R> extends ParameterDefinitionBody<R> {
        R accept(String p1, String p2) throws Throwable;
    }
    @FunctionalInterface
    interface A3<R> extends ParameterDefinitionBody<R> {
        R accept(String p1, String p2, String p3) throws Throwable;
    }
}
