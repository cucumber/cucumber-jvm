import org.openqa.selenium.By

this.metaClass.mixin(cuke4duke.GroovyDsl)
import static junit.framework.Assert.*

Given(~"I am on the Google search page") { ->
  browser.get("http://google.com/")
}

When(~"I search for \"(.*)\"") { String query ->
  searchField = browser.findElement(By.name("q"))
  searchField.sendKeys(query)
  // WebDriver will find the containing form for us from the searchField element
  searchField.submit()
}

Then(~"I should see") { String text -> 
  if(browser.getPageSource().indexOf(text) == -1) {
    fail("Didn't find " + text + " on the page")
  }
}
