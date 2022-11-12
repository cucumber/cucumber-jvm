package io.cucumber.core.order;

public interface PickleOrderFactory {

    PickleOrder create(String name, String argument);
}
