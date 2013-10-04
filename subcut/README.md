Cucumber-SubCut dependency injection
====================================

This project enables to use the SubCut dependency injection for Scala.
(https://github.com/dickwall/subcut)

provide a binding module
-------------------------
	
	import com.escalatesoft.subcut.inject.NewBindingModule
	import cucumber.runtime.scala.subcut.SubCutObjectFactory
	...
	
	object SubCutConfigurationModule extends NewBindingModule (module =>{
	  import module._
	  // bind some things:
	  bind [SingletonResource] toSingle new MySingletonResource
	  bind[WebClient] toProvider {WebClientProvider.provides()}
	  
	  // provide the binding module
	  bind[TestPage] toSingle new TestPage()(module)
	})

Inject the configured objects into your classes
-----------------------------------------------

- add implicit val bindingModule as extra parameter list
- extend Injectable
- inject objects
- (optional) add Before and After hooks

	import com.escalatesoft.subcut.inject.BindingModule
	import com.escalatesoft.subcut.inject.Injectable
	import cucumber.api.scala.ScalaDsl
	import cucumber.api.Scenario
	
	class TestDienstPage(implicit val bindingModule: BindingModule) extends Injectable with ScalaDsl {

	  var driver = inject[SharedWebDriver]	
	    
	  After { s: Scenario => driver.close() }
	}
	
technical notes
---------------

To be able to use injection 'frameworks' with the Cucumber-Scala port, it needed to
utilize an ObjectFactory similar to the java port. With this modification in place,
it should be straight forward to write adapters for other DI-Frameworks like Spring 
or Guice.

TODO: The SubCutObjectFactory searches the whole classpath for a BindingModule instance
As it supports only a single BindingModule, it should only look below the configured
paths in the glue (which it fails to do at the moment).