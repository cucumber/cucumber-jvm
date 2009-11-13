import org.openqa.selenium.By
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

this.metaClass.mixin(cuke4duke.GroovyDsl); Before([] as Object[]); After([] as Object[]) // HACK: http://jira.codehaus.org/browse/GROOVY-3878

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
  assertThat(browser.getPageSource(), containsString(text))
}

