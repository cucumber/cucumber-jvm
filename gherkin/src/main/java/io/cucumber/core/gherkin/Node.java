package io.cucumber.core.gherkin;

import java.util.function.BiFunction;

public interface Node extends Located, Named {

    default <T> T map(
        T parent,
        BiFunction<Feature, T, T> mapFeature,
        BiFunction<Scenario, T, T> mapScenario,
        BiFunction<Rule, T, T> mapRule,
        BiFunction<ScenarioOutline, T, T> mapScenarioOutline,
        BiFunction<Examples, T, T> mapExamples,
        BiFunction<Example, T, T> mapExample
    ) {
        if (this instanceof Scenario) {
            return mapScenario.apply((Scenario) this, parent);
        } else if (this instanceof Example) {
            return mapExample.apply((Example) this, parent);
        } else if (this instanceof Container){
            final T mapped;
            if (this instanceof Feature) {
                mapped = mapFeature.apply((Feature) this, parent);
            } else if(this instanceof Rule){
                mapped = mapRule.apply((Rule) this, parent);
            } else if (this instanceof ScenarioOutline){
                mapped = mapScenarioOutline.apply((ScenarioOutline) this, parent);
            } else if (this instanceof Examples){
                mapped = mapExamples.apply((Examples) this, parent);
            } else {
                throw new IllegalArgumentException(this.getClass().getName());
            }
            Container<?> container = (Container<?>) this;
            container.children().forEach(node -> node.map(mapped, mapFeature, mapScenario, mapRule, mapScenarioOutline, mapExamples, mapExample));
            return mapped;
        } else {
            throw new IllegalArgumentException(this.getClass().getName());
        }
    }
}
