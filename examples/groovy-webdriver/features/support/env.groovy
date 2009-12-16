//import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver

this.metaClass.mixin(cuke4duke.GroovyDsl)

Before() {
  //browser = new FirefoxDriver()
  browser = new HtmlUnitDriver()
}

After() {
  browser.close()
  browser.quit()
}
