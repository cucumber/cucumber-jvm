package cucumber.runtime.groovy

import static groovy.util.GroovyTestCase.assertEquals
import java.util.Date
import cucumber.api.Transformer
import cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter
import java.text.SimpleDateFormat

this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN)

@XStreamConverter(DateWrapperConverter)
class DateWrapper {
  def date
}

class DateWrapperConverter extends Transformer<DateWrapper> {
  def DateWrapper transform(String string) {
    def df = new SimpleDateFormat("dd-MM-yyyy")
    return new DateWrapper(date:df.parse(string));
  }
}

Given(~'^today\'s date is "(.*)"') { DateWrapper dw ->
    assertEquals(71, dw.date.year)
}
