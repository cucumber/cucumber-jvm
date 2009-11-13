package cuke4duke.webdriver;

import cuke4duke.Given;
import cuke4duke.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SearchPage {
    private final WebDriver d;

    public SearchPage(WebDriverFacade facade) {
        d = facade.getWebDriver();
    }

    @Given("I am on the Google search page")
    public void visit() {
        d.get("http://google.com/");
    }

    @When("^I search for \"([^\"]*)\"$")
    public void search(String query) {
        WebElement searchField = d.findElement(By.name("q"));
        searchField.sendKeys(query);
        // WebDriver will find the containing form for us from the searchField element
        searchField.submit();
    }
}