package cuke4duke.webdriver;

import cuke4duke.Then;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

public class ResultsPage {
    private final WebDriverFacade facade;

    public ResultsPage(WebDriverFacade facade) {
        this.facade = facade;
    }

    @Then("^I should see$")
    public void seeSearchResults(String results) {
        assertThat(facade.getBrowser().getPageSource(), containsString(results));
    }
}
