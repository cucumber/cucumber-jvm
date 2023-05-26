package io.cucumber.picocontainer;

import org.picocontainer.Disposable;

public class StepDefinitionsWithTransitiveDependencies {

    final FirstDependency firstDependency;

    public StepDefinitionsWithTransitiveDependencies(FirstDependency firstDependency) {
        this.firstDependency = firstDependency;
    }

    public static class FirstDependency implements Disposable {
        final SecondDependency secondDependency;

        public FirstDependency(SecondDependency secondDependency) {
            this.secondDependency = secondDependency;
        }

        @Override
        public void dispose() {
        }
    }

    public static class SecondDependency {

    }
}
