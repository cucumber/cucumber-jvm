package io.cucumber.examples.testng;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 * An example base class
 */
class RunCucumberByCompositionBase {

    @BeforeClass
    public void beforeClass() {
        // do expensive setup
    }

    @BeforeMethod
    public void beforeMethod() {
        // do expensive setup
    }

}
