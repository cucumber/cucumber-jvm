package subcut.loadmodule


import com.escalatesoft.subcut.inject.NewBindingModule
import cucumber.runtime.scala.subcut.SubCutObjectFactory

object SubCutConfigurationModule extends NewBindingModule (module =>{
  import module._
  
//  bind[SharedBetweenSteps] toProvider {new SharedBetweenSteps}
  bind[SharedBetweenSteps] toSingle new SharedBetweenSteps
})
