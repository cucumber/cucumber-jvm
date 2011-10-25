Feature: cucumber-guice loads modules passed by the user
    Scenario: Passes same instance to booth step classes
        Given a cucumber-guice.properties file at the classpath root
        And the properties file points to the module class 'cucumber.runtime.java.guice.loadguicemodule.YourModuleClass'
        And the class SharedBetweenSteps is bound to a single instance
        When the first step class visits the instance of SharedBetweenSteps
        Then the instance passed to the second step class is still visited