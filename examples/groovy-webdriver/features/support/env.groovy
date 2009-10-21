import org.openqa.selenium.firefox.FirefoxDriver

this.metaClass.mixin(cuke4duke.GroovyDsl)

Before([]) {
  browser = new FirefoxDriver()
}

After([]) {
  browser.close()
  browser.quit()
}
