package cucumber.runtime.java.guice.loadguicemodule;

import javax.inject.Inject;

import cucumber.annotation.Pending;
import cucumber.annotation.en.And;
import cucumber.annotation.en.Given;
import cucumber.annotation.en.When;

public class FirstSteps {
    
    private final SharedBetweenSteps shared;

    @Inject
    public FirstSteps(SharedBetweenSteps shared) {
        this.shared = shared;
    }
    
    @Given("^a cucumber-guice.properties file at the classpath root$")
    @Pending
    public void a_cucumber_guice_properties_file_at_the_classpath_root() {
        
    }
    
    @And("^the class SharedBetweenSteps is bound to a single instance$")
    @Pending
    public void the_class_SharedBetweenSteps_is_bound_to_a_single_instance() {
        // Express the Regexp above with the code you wish you had
    }

    @And("^the properties file points to the module class 'cucumber.runtime.java.guice.loadguicemodule.YourModuleClass'$")
    @Pending
    public void the_properties_file_points_to_the_module_class() {
        // Express the Regexp above with the code you wish you had
    }

    
    @When("^the first step class visits the instance of SharedBetweenSteps$")
    @Pending
    public void the_first_step_class_visits_the_instance_of_SharedBetweenSteps() {
        shared.visit();
    }
}