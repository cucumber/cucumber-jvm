package cucumber.runtime.junit.glue;

import cucumber.annotation.EN.Given;

public class Glue {
    @Given("I have (\\d+) cukes in my belly")
    public void cukesInTheBelly(String cukes) {
//        assertEquals("12", cukes);
    }
}
