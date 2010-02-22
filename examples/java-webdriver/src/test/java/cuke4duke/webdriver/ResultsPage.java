package cuke4duke.webdriver;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import cuke4duke.annotation.EN.*;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.InvocationTargetException;

public class ResultsPage {
    private final WebDriver d;

    public ResultsPage(WebDriverFacade facade) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        d = facade.getWebDriver();
    }

    @Then("^I should see$")
    public void shouldSee(String results) {
        assertThat(d.getPageSource(), containsString(results));
    }
}
