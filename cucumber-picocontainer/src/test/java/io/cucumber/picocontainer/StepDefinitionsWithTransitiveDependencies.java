package io.cucumber.picocontainer;

public class StepDefinitionsWithTransitiveDependencies {

    final FirstDependency firstDependency;

    public StepDefinitionsWithTransitiveDependencies(FirstDependency firstDependency) {
        this.firstDependency = firstDependency;
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
