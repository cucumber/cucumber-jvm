package subcut.loadmodule

import com.escalatesoft.subcut.inject.BindingModule
import com.escalatesoft.subcut.inject.Injectable
import cucumber.api.PendingException
import cucumber.api.scala.DE
import cucumber.api.scala.ScalaDsl
import cucumber.api.scala.EN

class StepDefinitions2(implicit val bindingModule: BindingModule) extends ScalaDsl with Injectable with EN {

  var shared = inject[SharedBetweenSteps]
  
  Then("""^the instance passed to the second step class is still visited$""") { () =>
    assert(shared.isVisited)
  }

}