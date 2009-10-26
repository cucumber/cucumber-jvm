package cuke4duke.webdriver;

import cuke4duke.Given;
import cuke4duke.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class SearchPage {
    private final WebDriverFacade facade;

    public SearchPage(WebDriverFacade facade) {
        this.facade = facade;
    }

    @Given("I am on the Google search page")
    public void visit() {
        facade.getBrowser().get("http://google.com/");
    }

    @When("^I search for \"([^\"]*)\"$")
    public void search(String query) {
        WebElement searchField = facade.getBrowser().findElement(By.name("q"));
        searchField.sendKeys(query);
        // WebDriver will find the containing form for us from the searchField element
        searchField.submit();
    }
}