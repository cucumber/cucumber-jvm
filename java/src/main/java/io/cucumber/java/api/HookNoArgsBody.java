package io.cucumber.java.api;

@FunctionalInterface
public interface HookNoArgsBody {
    void accept() throws Throwable;
}
