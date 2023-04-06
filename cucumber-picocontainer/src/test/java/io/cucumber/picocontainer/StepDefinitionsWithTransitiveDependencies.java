package io.cucumber.picocontainer;

import io.cucumber.java.en.Given;

public class StepDefinitionsWithTransitiveDependencies {

    final FirstDependency firstDependency;

    public StepDefinitionsWithTransitiveDependencies(FirstDependency firstDependency) {
        this.firstDependency = firstDependency;
    }

    @Given("step definitions with transitive dependencies")
    public void stepDefinitionsWithTransitiveDependencies(){

    }

    public static class FirstDependency {
        final SecondDependency secondDependency;

        public FirstDependency(SecondDependency secondDependency) {
            this.secondDependency = secondDependency;
        }
    }

    public static class SecondDependency {

    }
}
