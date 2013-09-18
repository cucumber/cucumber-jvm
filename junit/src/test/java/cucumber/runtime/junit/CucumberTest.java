package cucumber.runtime.junit;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;

import cucumber.annotation.DummyWhen;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import cucumber.runtime.CucumberException;

public class CucumberTest {

    private String dir;

    @Before
    public void ensureDirectory() {
        dir = System.getProperty("user.dir");
        if (dir.endsWith("cucumber-jvm")) {
            // Might be the case if we're running in an IDE - at least in IDEA.
            System.setProperty("user.dir", new File(dir, "junit").getAbsolutePath());
        }
    }

    @After
    public void ensureOriginalDirectory() {
        System.setProperty("user.dir", dir);
    }

    @Test
    public void finds_features_based_on_implicit_package() throws IOException, InitializationError {
        Cucumber cucumber = new Cucumber(ImplicitFeatureAndGluePath.class);
        assertEquals(4, cucumber.getChildren().size());
        assertEquals("FA", cucumber.getChildren().get(0).getName());
    }

    @Test
    public void finds_features_based_on_explicit_root_package() throws IOException, InitializationError {
        Cucumber cucumber = new Cucumber(ExplicitFeaturePath.class);
        assertEquals(4, cucumber.getChildren().size());
        assertEquals("FA", cucumber.getChildren().get(0).getName());
    }

    @Test
    public void allRunnersShouldHaveUniqueDescription() throws Exception {
        Cucumber cucumber = new Cucumber(ExplicitFeaturePath.class);
        Set<Description> descriptions = getRunnerDescriptions(cucumber.getDescription());
        // one for each scenario and example
        assertEquals(7, descriptions.size());
    }

    private Set<Description> getRunnerDescriptions(Description description) {
        Set<Description> result = new HashSet<Description>();
        if (description.isSuite()) {
            for (Description child : description.getChildren()) {
                result.addAll(getRunnerDescriptions(child));
            }
        } else {
            result.add(description);
        }
        return result;
    }


    @Test
    public void testThatParsingErrorsIsNicelyReported() throws Exception {
        try {
            new Cucumber(LexerErrorFeature.class);
            fail("Expecting error");
        } catch (CucumberException e) {
            assertEquals("Error parsing feature file cucumber/runtime/error/lexer_error.feature", e.getMessage());
        }
    }

    @Test
    public void finds_no_features_when_explicit_feature_path_has_no_features() throws IOException, InitializationError {
        Cucumber cucumber = new Cucumber(ExplicitFeaturePathWithNoFeatures.class);
        assertEquals(emptyList(), cucumber.getChildren());
    }

    @RunWith(Cucumber.class)
    private class RunCukesTestValidEmpty {
    }

    @RunWith(Cucumber.class)
    private class RunCukesTestValidIgnored {
        public void ignoreMe() {
        }
    }

    @RunWith(Cucumber.class)
    private class RunCukesTestInvalid {
        @DummyWhen
        public void ignoreMe() {
        }
    }

    @Test
    public void no_stepdefs_in_cucumber_runner_valid() {
        Assertions.assertNoCucumberAnnotatedMethods(RunCukesTestValidEmpty.class);
        Assertions.assertNoCucumberAnnotatedMethods(RunCukesTestValidIgnored.class);
    }

    @Test(expected = CucumberException.class)
    public void no_stepdefs_in_cucumber_runner_invalid() {
        Assertions.assertNoCucumberAnnotatedMethods(RunCukesTestInvalid.class);
    }

    private class ImplicitFeatureAndGluePath {
    }

    @CucumberOptions(features = {"classpath:cucumber/runtime/junit"})
    private class ExplicitFeaturePath {
    }

    @CucumberOptions(features = {"classpath:gibber/ish"})
    private class ExplicitFeaturePathWithNoFeatures {
    }

    @CucumberOptions(features = {"classpath:cucumber/runtime/error/lexer_error.feature"})
    private class LexerErrorFeature {

    }
}
