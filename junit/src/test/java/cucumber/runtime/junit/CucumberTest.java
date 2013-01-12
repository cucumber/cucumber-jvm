package cucumber.runtime.junit;

import cucumber.annotation.DummyWhen;
import cucumber.api.junit.Cucumber;
import cucumber.runtime.CucumberException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

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
        Cucumber cucumber = new Cucumber(ImplicitFeaturePath.class);
        assertEquals(3, cucumber.getChildren().size());
        assertEquals("Feature: FA", cucumber.getChildren().get(0).getName());
    }

    @Test
    public void finds_features_based_on_explicit_root_package() throws IOException, InitializationError {
        Cucumber cucumber = new Cucumber(ExplicitFeaturePath.class);
        assertEquals(3, cucumber.getChildren().size());
        assertEquals("Feature: FA", cucumber.getChildren().get(0).getName());
    }

    @Test(expected = CucumberException.class)
    public void finds_no_features_when_explicit_package_has_nothnig() throws IOException, InitializationError {
        new Cucumber(ExplicitFeaturePathWithNoFeatures.class);
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
