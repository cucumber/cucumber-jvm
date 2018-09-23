package io.cucumber.examples.java.calculator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 *  An example base class
 */
class RunCukesByCompositionBase {
    
    @BeforeClass
    public void beforeClass() {
        // do expensive setup
    }

    @BeforeMethod
    public void beforeMethod() {
        // do expensive setup
    }
}
