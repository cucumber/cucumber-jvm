package cucumber.runtime.io;

import org.junit.Rule;
import org.junit.Test;
import org.osgi.framework.Bundle;

import java.util.NoSuchElementException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mdelapenya
 */
// https://github.com/cucumber/cucumber-jvm/issues/900
public class BundleResourceIteratorTest {

	@Rule
	public ModuleFrameworkTestRule moduleFrameworkTestRule =
		new ModuleFrameworkTestRule();

	@Test
	public void has_next_returns_true_if_bundle_resource_has_elements() {
		Bundle testBundle = moduleFrameworkTestRule.getTestBundle();

		BundleResourceIterator bundleResourceIterator =
			new BundleResourceIterator(testBundle, "class");

		assertTrue(bundleResourceIterator.hasNext());

		Resource next = bundleResourceIterator.next();

		assertTrue(next instanceof BundleResource);
	}

	@Test(expected = NoSuchElementException.class)
	public void has_next_returns_false_if_bundle_resource_has_no_elements() {
		Bundle testBundle = moduleFrameworkTestRule.getTestBundle();

		BundleResourceIterator bundleResourceIterator =
			new BundleResourceIterator(testBundle, "undefined_extension");

		assertFalse(bundleResourceIterator.hasNext());

		bundleResourceIterator.next();
	}

}
