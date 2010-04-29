package simple;

import cuke4duke.annotation.I18n.EN.When;
import cuke4duke.annotation.I18n.NO.Så;

import static org.junit.Assert.assertEquals;

public class NorwegianSteps {
    @When("^Jæ (.+) ålsker (.+) lændet$")
    public void jæViElsker(String hva, String hae) {
    }

    @Så("skal (.+) Double bli parset riktig")
    public void numberShouldBeParserCorrectly(Double d) {
        assertEquals(10.4, d, 0.0);
    }
}
