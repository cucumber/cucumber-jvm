package io.cucumber.java.stepswithinterface;

import io.cucumber.java.en.Given;

/**
 * Having a class which implements a parent class will lead to duplicate the
 * methods which return specialized type (here Number for the interface and
 * Integer for the subclass). The original method has a "volatile" modifier.
 */
public class StepsWithInterface implements StepsInterface {

    @Given("test")
    public Integer test() {
        return 1;
    }

}
