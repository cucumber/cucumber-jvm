package cucumber.junit;

import cucumber.runtime.CucumberException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CucumberTest {

    private String dir;

    // TODO: While on the plain I couldn't look up how to change directory in Java... Fix this.
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
        Cucumber cucumber = new Cucumber(ImplicitPackage.class);
        assertEquals(2, cucumber.getChildren().size());
        assertEquals("Feature: In cucumber.junit", cucumber.getChildren().get(0).getName());
    }

    @Test
    public void finds_features_based_on_explicit_root_package() throws IOException, InitializationError {
        Cucumber cucumber = new Cucumber(ExplicitPackage.class);
        assertEquals(2, cucumber.getChildren().size());
        assertEquals("Feature: In cucumber.junit", cucumber.getChildren().get(0).getName());
    }

    @Test(expected = CucumberException.class)
    public void finds_no_features_when_explicit_package_has_nothnig() throws IOException, InitializationError {
        new Cucumber(ExplicitPackageWithNoFeatures.class);
    }

    private class ImplicitPackage {
    }

    @Cucumber.Options(features={"cucumber/junit"})
    private class ExplicitPackage {
    }

    @Cucumber.Options(features={"gibber/ish"})
    private class ExplicitPackageWithNoFeatures {
    }
}
