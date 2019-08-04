package io.cucumber.core.runner;

import gherkin.pickles.PickleStep;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AmbiguousStepDefinitionsExceptionTest {

    @TestFactory
    public Collection<DynamicTest> exceptions() {

        return Arrays.asList(

            DynamicTest.dynamicTest("Null PickleStep, Null PickleStepDefinitionMatches Collection", () -> {
                final Executable testMethod = () -> {
                    throw new AmbiguousStepDefinitionsException(null, null);
                };
                final AmbiguousStepDefinitionsException expectedThrown = assertThrows(AmbiguousStepDefinitionsException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo("Null PickleStep matches more than one step definition:\n"))),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue())),
                    () -> assertThat(expectedThrown.getMatches(), is(equalTo(new ArrayList<Throwable>())))
                );
            }),

            DynamicTest.dynamicTest("PickleStep, Empty PickleStepDefinitionMatches Collection", () -> {
                final PickleStep mockPickleStep = mock(PickleStep.class);
                when(mockPickleStep.getText())
                    .thenReturn("PickleStep_Text");
                final Executable testMethod = () -> {
                    throw new AmbiguousStepDefinitionsException(mockPickleStep, new ArrayList<>());
                };
                final AmbiguousStepDefinitionsException expectedThrown = assertThrows(AmbiguousStepDefinitionsException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo("\"PickleStep_Text\" matches more than one step definition:\n"))),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue())),
                    () -> assertThat(expectedThrown.getMatches(), is(equalTo(new ArrayList<Throwable>())))
                );
            }),

            DynamicTest.dynamicTest("PickleStep, PickleStepDefinitionMatches Collection of One", () -> {
                final PickleStep mockPickleStep = mock(PickleStep.class);
                when(mockPickleStep.getText())
                    .thenReturn("PickleStep_Text");
                final List<PickleStepDefinitionMatch> matches = new ArrayList<>();
                final PickleStepDefinitionMatch mockPickleStepDefinitionMatchOne = mock(PickleStepDefinitionMatch.class);
                when(mockPickleStepDefinitionMatchOne.getPattern())
                    .thenReturn("PickleStepDefinitionMatchOne_Pattern");
                when(mockPickleStepDefinitionMatchOne.getLocation())
                    .thenReturn("PickleStepDefinitionMatchOne_Location");
                matches.add(mockPickleStepDefinitionMatchOne);
                final Executable testMethod = () -> {
                    throw new AmbiguousStepDefinitionsException(mockPickleStep, matches);
                };
                final AmbiguousStepDefinitionsException expectedThrown = assertThrows(AmbiguousStepDefinitionsException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo(
                        "\"PickleStep_Text\" matches more than one step definition:\n  \"PickleStepDefinitionMatchOne_Pattern\" in PickleStepDefinitionMatchOne_Location\n"
                    ))),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue())),
                    () -> assertThat(expectedThrown.getMatches(), is(equalTo(matches)))
                );
            }),

            DynamicTest.dynamicTest("PickleStep, PickleStepDefinitionMatches Collection of Two", () -> {
                final PickleStep mockPickleStep = mock(PickleStep.class);
                when(mockPickleStep.getText())
                    .thenReturn("PickleStep_Text");
                final List<PickleStepDefinitionMatch> matches = new ArrayList<>();
                final PickleStepDefinitionMatch mockPickleStepDefinitionMatchOne = mock(PickleStepDefinitionMatch.class);
                when(mockPickleStepDefinitionMatchOne.getPattern())
                    .thenReturn("PickleStepDefinitionMatchOne_Pattern");
                when(mockPickleStepDefinitionMatchOne.getLocation())
                    .thenReturn("PickleStepDefinitionMatchOne_Location");
                matches.add(mockPickleStepDefinitionMatchOne);
                final PickleStepDefinitionMatch mockPickleStepDefinitionMatchTwo = mock(PickleStepDefinitionMatch.class);
                when(mockPickleStepDefinitionMatchTwo.getPattern())
                    .thenReturn("PickleStepDefinitionMatchTwo_Pattern");
                when(mockPickleStepDefinitionMatchTwo.getLocation())
                    .thenReturn("PickleStepDefinitionMatchTwo_Location");
                matches.add(mockPickleStepDefinitionMatchTwo);
                final Executable testMethod = () -> {
                    throw new AmbiguousStepDefinitionsException(mockPickleStep, matches);
                };
                final AmbiguousStepDefinitionsException expectedThrown = assertThrows(AmbiguousStepDefinitionsException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo(
                        "\"PickleStep_Text\" matches more than one step definition:\n  \"PickleStepDefinitionMatchOne_Pattern\" in PickleStepDefinitionMatchOne_Location\n  \"PickleStepDefinitionMatchTwo_Pattern\" in PickleStepDefinitionMatchTwo_Location\n"
                    ))),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue())),
                    () -> assertThat(expectedThrown.getMatches(), is(equalTo(matches)))
                );
            })

        );
    }

}
