package io.cucumber.java8;

import org.apiguardian.api.API;

@FunctionalInterface
@API(status = API.Status.STABLE)
public interface HookNoArgsBody {

    void accept() throws Throwable;

}
