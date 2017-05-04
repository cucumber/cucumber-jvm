package cucumber.runtime.io;

import org.junit.Rule;
import org.junit.Test;

import org.osgi.framework.Bundle;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author mdelapenya
 */
// https://github.com/cucumber/cucumber-jvm/issues/900
public class BundleResourceTest {

    @Rule
    public ModuleFrameworkTestRule moduleFrameworkTestRule =
        new ModuleFrameworkTestRule();

    @Test
    public void get_absolute_path_should_return_bundle_url_plus_bundle_entry()
        throws MalformedURLException {

        ClassLoader classLoader = getClass().getClassLoader();

        URL bundleURL = classLoader.getResource("cucumber/runtime");
        URL entryURL = classLoader.getResource("cucumber/runtime/bar.properties");

        BundleResource toTest = new BundleResource(bundleURL, entryURL);

        assertEquals(
            bundleURL.getPath().concat(entryURL.getPath()),
            toTest.getAbsolutePath());
    }

    @Test
    public void get_path_should_return_entry_url()
        throws MalformedURLException {

        ClassLoader classLoader = getClass().getClassLoader();

        URL bundleURL = classLoader.getResource("cucumber/runtime");
        URL entryURL = classLoader.getResource("cucumber/runtime/bar.properties");

        BundleResource toTest = new BundleResource(bundleURL, entryURL);

        assertEquals(entryURL.getPath(), toTest.getPath());
    }

    @Test
    public void get_classname_should_throw_exception_when_bundle_entry_is_null()
        throws MalformedURLException {

        ClassLoader classLoader = getClass().getClassLoader();

        URL bundleURL = classLoader.getResource(
            "cucumber/runtime/http-servlet.jar");

        Bundle bundle = moduleFrameworkTestRule.getTestBundle();

        URL entryURL = bundle.getEntry(
            "not.found.package.NotFound.class");

        BundleResource toTest = new BundleResource(bundleURL, entryURL);

        try {
            toTest.getClassName("ext");

            fail("An exception should have been thrown");
        }
        catch(IllegalArgumentException iae) {
            assertEquals(
                "The resource does not exist in the bundle", iae.getMessage());
        }
    }

    @Test
    public void get_classname_should_throw_exception_when_bundle_entry_url_is_invalid()
        throws MalformedURLException {

        ClassLoader classLoader = getClass().getClassLoader();

        URL bundleURL = classLoader.getResource(
            "cucumber/runtime/http-servlet.jar");

        URL entryURL = classLoader.getResource("cucumber/runtime/foo.properties");

        BundleResource toTest = new BundleResource(bundleURL, entryURL);

        try {
            toTest.getClassName("ext");

            fail("An exception should have been thrown");
        }
        catch(IllegalArgumentException iae) {
            assertEquals(
                "The resource is not a valid bundle resource", iae.getMessage());
        }
    }

    @Test
    public void get_classname_should_return_classname()
        throws MalformedURLException {

        ClassLoader classLoader = getClass().getClassLoader();

        URL bundleURL = classLoader.getResource(
            "cucumber/runtime/http-servlet.jar");

        Bundle bundle = moduleFrameworkTestRule.getTestBundle();

        URL entryURL = bundle.getEntry(
            "org/eclipse/equinox/http/servlet/HttpServiceServlet.class");

        BundleResource toTest = new BundleResource(bundleURL, entryURL);

        assertEquals(
            "org.eclipse.equinox.http.servlet.HttpServiceServlet.class",
            toTest.getClassName("never-mind-extension"));
    }

}
