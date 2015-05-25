package subcut.loadmodule

import com.escalatesoft.subcut.inject.BindingModule
import com.escalatesoft.subcut.inject.Injectable
import cucumber.api.PendingException
import cucumber.api.scala.DE
import cucumber.api.scala.ScalaDsl
import cucumber.api.scala.EN

class StepDefinitions(implicit val bindingModule: BindingModule) extends ScalaDsl with Injectable with EN {

  var shared = inject[SharedBetweenSteps]
  
  Given("""^the class SharedBetweenSteps is bound to a single instance$""") { () =>
    // this is defined in the SubCutConfigurationModule
  }
  When("""^the first step class visits the instance of SharedBetweenSteps$""") { () =>
    shared.visit
  }
 

}