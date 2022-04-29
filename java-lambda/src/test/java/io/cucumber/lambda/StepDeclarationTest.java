package io.cucumber.lambda;

import org.junit.jupiter.api.Test;

import java.util.List;

import static io.cucumber.lambda.StepDefinitions.using;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class StepDeclarationTest {

    // @formatter:off
    final StepDefinitions allStepDefinitions = using(World.class)
            .step("0 arguments",
                    (World world)
                            -> ()
                            -> world.zero())
            .step("1 arguments {int}",
                    (World world) -> (Integer one)
                            -> world.one(one))
            .step("2 arguments {int}, {int}",
                    (World world)
                            -> (Integer one, Integer two)
                            -> world.two(one, two))
            .step("3 arguments {int}, {int}, {int}",
                    (World world)
                            -> (Integer one, Integer two, Integer three)
                            -> world.three(one, two, three))
            .step("4 arguments {int}, {int}, {int}, {int}",
                    (World world)
                            -> (Integer one, Integer two, Integer three, Integer four)
                            -> world.four(one, two, three, four))
            .step("5 arguments {int}, {int}, {int}, {int}, {int}",
                    (World world)
                            -> (Integer one, Integer two, Integer three, Integer four, Integer five)
                            -> world.five(one, two, three, four, five))
            .step("6 arguments {int}, {int}, {int}, {int}, {int}, {int}",
                    (World world)
                            -> (Integer one, Integer two, Integer three, Integer four, Integer five, Integer six)
                            -> world.six(one, two, three, four, five, six))
            .step("7 arguments {int}, {int}, {int}, {int}, {int}, {int}, {int}",
                    (World world)
                            -> (Integer one, Integer two, Integer three, Integer four, Integer five, Integer six, Integer seven)
                            -> world.seven(one, two, three, four, five, six, seven))
            .step("8 arguments {int}, {int}, {int}, {int}, {int}, {int}, {int}, {int}",
                    (World world)
                            -> (Integer one, Integer two, Integer three, Integer four, Integer five, Integer six, Integer seven, Integer eight)
                            -> world.eight(one, two, three, four, five, six, seven, eight))
            .step("9 arguments {int}, {int}, {int}, {int}, {int}, {int}, {int}, {int}, {int}",
                    (World world)
                            -> (Integer one, Integer two, Integer three, Integer four, Integer five, Integer six, Integer seven, Integer eight, Integer nine)
                            -> world.nine(one, two, three, four, five, six, seven, eight, nine))
            .build();
    // @formatter:on

    @Test
    void knowsItsLocationInAFile() {
        assertThat(allStepDefinitions.getStepDeclarations())
                .extracting(StepDeclaration::getLocation)
                .containsExactly(
                        "io.cucumber.lambda.StepDeclarationTest.<init>(StepDeclarationTest.java:17)",
                        "io.cucumber.lambda.StepDeclarationTest.<init>(StepDeclarationTest.java:21)",
                        "io.cucumber.lambda.StepDeclarationTest.<init>(StepDeclarationTest.java:24)",
                        "io.cucumber.lambda.StepDeclarationTest.<init>(StepDeclarationTest.java:28)",
                        "io.cucumber.lambda.StepDeclarationTest.<init>(StepDeclarationTest.java:32)",
                        "io.cucumber.lambda.StepDeclarationTest.<init>(StepDeclarationTest.java:36)",
                        "io.cucumber.lambda.StepDeclarationTest.<init>(StepDeclarationTest.java:40)",
                        "io.cucumber.lambda.StepDeclarationTest.<init>(StepDeclarationTest.java:44)",
                        "io.cucumber.lambda.StepDeclarationTest.<init>(StepDeclarationTest.java:48)",
                        "io.cucumber.lambda.StepDeclarationTest.<init>(StepDeclarationTest.java:52)"
                );
    }


    @Test
    void hasAContextClass() {
        assertThat(allStepDefinitions.getStepDeclarations())
                .extracting(StepDeclaration::getContext)
                .map(Class.class::cast)
                .containsOnly(World.class);
    }

    @Test
    void hasAnExpression() {
        List<StepDeclaration> declarations = allStepDefinitions.getStepDeclarations();
        assertThat(declarations)
                .extracting(StepDeclaration::getExpression)
                .containsExactly(
                        "0 arguments",
                        "1 arguments {int}",
                        "2 arguments {int}, {int}",
                        "3 arguments {int}, {int}, {int}",
                        "4 arguments {int}, {int}, {int}, {int}",
                        "5 arguments {int}, {int}, {int}, {int}, {int}",
                        "6 arguments {int}, {int}, {int}, {int}, {int}, {int}",
                        "7 arguments {int}, {int}, {int}, {int}, {int}, {int}, {int}",
                        "8 arguments {int}, {int}, {int}, {int}, {int}, {int}, {int}, {int}",
                        "9 arguments {int}, {int}, {int}, {int}, {int}, {int}, {int}, {int}, {int}");
    }
    @Test
    void supportsUpToNineArguments() {
        List<StepDeclaration> declarations = allStepDefinitions.getStepDeclarations();
        World world = new World();
        assertAll(
                // @formatter:off
                () -> assertDoesNotThrow(() -> declarations.get(0).invoke(world, new Integer[] { })),
                () -> assertDoesNotThrow(() -> declarations.get(1).invoke(world, new Integer[] { 1 })),
                () -> assertDoesNotThrow(() -> declarations.get(2).invoke(world, new Integer[] { 1, 2 })),
                () -> assertDoesNotThrow(() -> declarations.get(3).invoke(world, new Integer[] { 1, 2, 3 })),
                () -> assertDoesNotThrow(() -> declarations.get(4).invoke(world, new Integer[] { 1, 2, 3, 4 })),
                () -> assertDoesNotThrow(() -> declarations.get(5).invoke(world, new Integer[] { 1, 2, 3, 4, 5 })),
                () -> assertDoesNotThrow(() -> declarations.get(6).invoke(world, new Integer[] { 1, 2, 3, 4, 5, 5 })),
                () -> assertDoesNotThrow(() -> declarations.get(7).invoke(world, new Integer[] { 1, 2, 3, 4, 5, 5, 7 })),
                () -> assertDoesNotThrow(() -> declarations.get(8).invoke(world, new Integer[] { 1, 2, 3, 4, 5, 5, 7, 8 })),
                () -> assertDoesNotThrow(() -> declarations.get(9).invoke(world, new Integer[] { 1, 2, 3, 4, 5, 5, 7, 8, 9 }))
                // @formatter:on
        );
    }

    public static class World {
        // @formatter:off
        public void zero() {

        }

        public void one(Integer one) {
            requireNonNull(one);
        }

        public void two(Integer one, Integer two) {
            requireNonNull(one);
            requireNonNull(two);
        }

        public void three(Integer one, Integer two, Integer three) {
            requireNonNull(one);
            requireNonNull(two);
            requireNonNull(three);
        }

        public void four(Integer one, Integer two, Integer three, Integer four) {
            requireNonNull(one);
            requireNonNull(two);
            requireNonNull(three);
            requireNonNull(four);
        }

        public void five(Integer one, Integer two, Integer three, Integer four, Integer five) {
            requireNonNull(one);
            requireNonNull(two);
            requireNonNull(three);
            requireNonNull(four);
            requireNonNull(five);
        }

        public void six(Integer one, Integer two, Integer three, Integer four, Integer five, Integer six) {
            requireNonNull(one);
            requireNonNull(two);
            requireNonNull(three);
            requireNonNull(four);
            requireNonNull(five);
            requireNonNull(six);
        }

        public void seven(Integer one, Integer two, Integer three, Integer four, Integer five, Integer six, Integer seven) {
            requireNonNull(one);
            requireNonNull(two);
            requireNonNull(three);
            requireNonNull(four);
            requireNonNull(five);
            requireNonNull(six);
            requireNonNull(seven);
        }

        public void eight(Integer one, Integer two, Integer three, Integer four, Integer five, Integer six, Integer seven, Integer eight) {
            requireNonNull(one);
            requireNonNull(two);
            requireNonNull(three);
            requireNonNull(four);
            requireNonNull(five);
            requireNonNull(six);
            requireNonNull(seven);
            requireNonNull(eight);
        }

        public void nine(Integer one, Integer two, Integer three, Integer four, Integer five, Integer six, Integer seven, Integer eight, Integer nine) {
            requireNonNull(one);
            requireNonNull(two);
            requireNonNull(three);
            requireNonNull(four);
            requireNonNull(five);
            requireNonNull(six);
            requireNonNull(seven);
            requireNonNull(eight);
            requireNonNull(nine);
        }
        // @formatter:on
    }

}
