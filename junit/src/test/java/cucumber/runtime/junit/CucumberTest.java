package cucumber.runtime.junit;

import cucumber.annotation.DummyWhen;
import cucumber.api.junit.Cucumber;
import cucumber.runtime.CucumberException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CucumberTest {

    @Test
    public void finds_features_based_on_implicit_package() throws IOException, InitializationError {
        Cucumber cucumber = new Cucumber(ImplicitFeaturePath.class);
        assertEquals(2, cucumber.getChildren().size());
        assertEquals("Feature: FA", cucumber.getChildren().get(0).getName());
    }

    @Test
    public void finds_features_based_on_explicit_root_package() throws IOException, InitializationError {
        Cucumber cucumber = new Cucumber(ExplicitFeaturePath.class);
        assertEquals(2, cucumber.getChildren().size());
        assertEquals("Feature: FA", cucumber.getChildren().get(0).getName());
    }

    @Test(expected = CucumberException.class)
    public void finds_no_features_when_explicit_package_has_nothing() throws IOException, InitializationError {
        new Cucumber(ExplicitFeaturePathWithNoFeatures.class);
    }

    @Test
    public void only_run_the_feature_indicated_by_the_starters_classname() throws Exception {
        Cucumber cucumber = new Cucumber(fb.class);
        assertEquals(1, cucumber.getChildren().size());
        assertEquals("Feature: FB", cucumber.getChildren().get(0).getName());
    }

    @Cucumber.Options(appendStarterClassToFeaturePaths = true)
    private class fb {

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

    private class ImplicitFeaturePath {
    }

    @Cucumber.Options(features = {"classpath:cucumber/runtime/junit"})
    private class ExplicitFeaturePath {
    }

    @Cucumber.Options(features = {"classpath:gibber/ish"})
    private class ExplicitFeaturePathWithNoFeatures {
    }
}
