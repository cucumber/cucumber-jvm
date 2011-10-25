package cucumber.runtime.java.guice.loadguicemodule;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Properties;

import javax.inject.Inject;

import cucumber.annotation.en.And;
import cucumber.annotation.en.Given;
import cucumber.annotation.en.When;
import cucumber.runtime.java.guice.UrlPropertiesLoader;

public class FirstSteps {
    
    private final SharedBetweenSteps shared;
    private Properties guiceProperties;

    @Inject
    public FirstSteps(SharedBetweenSteps shared) {
        this.shared = shared;
    }
    
    @Given("^a cucumber-guice.properties file at the classpath root$")
    public void a_cucumber_guice_properties_file_at_the_classpath_root() {
        UrlPropertiesLoader loader = new UrlPropertiesLoader();
        guiceProperties = loader.load(getClass().getClassLoader().getResource("cucumber-guice.properties"));
    }
    
    @And("^the properties file points to the module class 'cucumber.runtime.java.guice.loadguicemodule.YourModuleClass'$")
    public void the_properties_file_points_to_the_module_class() {
        String moduleClass = guiceProperties.getProperty("guiceModule");
        assertThat(moduleClass, is("cucumber.runtime.java.guice.loadguicemodule.YourModuleClass"));
    }
    
    @And("^the class SharedBetweenSteps is bound to a single instance$")
    public void the_class_SharedBetweenSteps_is_bound_to_a_single_instance() {
        //have a look at the module class
    }

    @When("^the first step class visits the instance of SharedBetweenSteps$")
    public void the_first_step_class_visits_the_instance_of_SharedBetweenSteps() {
        shared.visit();
    }
}