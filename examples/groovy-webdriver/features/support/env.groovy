//import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver

this.metaClass.mixin(cuke4duke.GroovyDsl); Before([] as Object[]); After([] as Object[]) // HACK: http://jira.codehaus.org/browse/GROOVY-3878

Before() {
  //browser = new FirefoxDriver()
  browser = new HtmlUnitDriver()
}

After() {
  browser.close()
  browser.quit()
}
