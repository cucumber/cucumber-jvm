package cucumber.java.runtime.osgi;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.eclipse.osgi.container.ModuleContainer;
import org.eclipse.osgi.container.ModuleDatabase;
import org.eclipse.osgi.internal.framework.EquinoxContainerAdaptor;
import org.eclipse.osgi.storage.url.BundleResourceHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleRevision;

public class OsgiClassFinderTest {

	private static final String PKG_NAME_SLASH = "java/util";
	private static final String PKG_NAME_DOT = "java.util";
	private static final String URL_BUNDLE_PROTOCOL = "bundleentry";
	private static final String URL_HOST_ID = "2.fwk1747702724";
	private static final String URL_FRAGMENT_ID = "5.fwk1747702724";
	private static final String[] HOST_CLASS_NAMES = { "ArrayList", "Vector", "HashMap" };
	private static final String[] FRAGMENT_CLASS_NAMES = { "Properties", "StringTokenizer", "Timer" };
	
	private OsgiClassFinder testSubject;
	private BundleContext mockBundleContext;
	private Bundle mockBundle;
	private Bundle mockFragmentBundle;

	
	@Before
	public void setUp() throws Exception {
		
		mockBundleContext = mock(BundleContext.class);
		mockBundle = mock(Bundle.class);
		mockFragmentBundle = mock(Bundle.class);
		
		prepURLs();
		
		// Build Host Bundle
		BundleRevision mockBundleRevision = mock(BundleRevision.class);
		when(mockBundleRevision.getTypes()).thenReturn(0);
		when(mockBundle.adapt(BundleRevision.class)).thenReturn(mockBundleRevision);
		Enumeration<URL> hostBundleEntries = buildHostBundleEntries();
		Enumeration<URL> fragmentBundleEntries = buildFragmentBundleEntries();
		when(mockBundle.findEntries(PKG_NAME_SLASH, "*.class", true)).thenReturn(hostBundleEntries);
		when(mockBundle.findEntries("target/classes/" + PKG_NAME_SLASH, "*.class", true)).thenReturn(fragmentBundleEntries);
		
		List<String> allClazzes = new ArrayList<String>();
		for (String clazz : HOST_CLASS_NAMES) {
			allClazzes.add(PKG_NAME_DOT + "." + clazz);
		}
		for (String clazz : FRAGMENT_CLASS_NAMES) {
			allClazzes.add(PKG_NAME_DOT + "." + clazz);
		}
		
		for (String clazzName : allClazzes) {
			Class clazz = Class.forName(clazzName, false, ClassLoader.getSystemClassLoader());
			when(mockBundle.loadClass(clazzName)).thenReturn(clazz);
		}
		
		
		// Build Fragment Bundle
		BundleRevision mockFragmentBundleRevision = mock(BundleRevision.class);
		when(mockFragmentBundleRevision.getTypes()).thenReturn(BundleRevision.TYPE_FRAGMENT);
		when(mockFragmentBundle.adapt(BundleRevision.class)).thenReturn(mockFragmentBundleRevision);
		
		when(mockBundleContext.getBundles()).thenReturn(new Bundle[]{mockBundle, mockFragmentBundle});
		
		testSubject = new OsgiClassFinder(mockBundleContext);
	}
	
	
	private void prepURLs() {
		
		try {
			URL.setURLStreamHandlerFactory(new BundleURLStreamHandlerFactoryImpl());
		} 
		catch (Error e) {
			// Do nothing
		}
	}
	
	
	private Enumeration<URL> buildHostBundleEntries () {
		
		Vector<URL> entries = new Vector<URL>();
		
		for (String clazzName : HOST_CLASS_NAMES) {
			try {
				URL url = new URL(URL_BUNDLE_PROTOCOL, URL_HOST_ID, "/" + PKG_NAME_SLASH + "/" + clazzName + ".class");
				entries.add(url);
			} 
			catch (MalformedURLException e) {
				Assert.fail("Failed to build URL in mock bundle. " + e.toString());
				e.printStackTrace();
			}
		}

		return entries.elements();
	}
	
	
	private Enumeration<URL> buildFragmentBundleEntries () {
		
		Vector<URL> entries = new Vector<URL>();

		// StepDef classes
		for (String clazzName : FRAGMENT_CLASS_NAMES) {
			try {
				URL url = new URL(URL_BUNDLE_PROTOCOL, URL_FRAGMENT_ID, "/target/classes/" + PKG_NAME_SLASH + "/" + clazzName + ".class");
				entries.add(url);
			} 
			catch (MalformedURLException e) {
				Assert.fail("Failed to build URL in mock bundle. " + e.toString());
				e.printStackTrace();
			}
		}

		return entries.elements();
	}

	
	@Test
	public void test_getDescendents_package_exists() {
		Collection<Class<? extends Object>> clazzes = testSubject.getDescendants(Object.class, PKG_NAME_DOT);
		
		// Classes should never try to be loaded directly from the fragment
		verify(mockFragmentBundle, never()).findEntries(anyString(), anyString(), anyBoolean());
		
		// All classes and StepDefs should have been found
		Assert.assertEquals("Classes not found in valid package", 6, clazzes.size());
	}
	
	
	@Test
	public void test_getDescendents_package_does_not_exist() {
		Collection<Class<? extends Object>> clazzes = testSubject.getDescendants(Object.class, "invalid.package.name");
		Assert.assertEquals("Classes found in invalid package", 0, clazzes.size());
	}
	
	
	private class BundleURLStreamHandlerFactoryImpl implements java.net.URLStreamHandlerFactory {

		@Override
		public URLStreamHandler createURLStreamHandler(String protocol) {
			
			EquinoxContainerAdaptor containerAdaptor = mock(EquinoxContainerAdaptor.class);
			ModuleDatabase modDatabase = new ModuleDatabase(containerAdaptor);
			ModuleContainer modContainer = new ModuleContainer(containerAdaptor, modDatabase);
			
			if (BundleResourceHandler.OSGI_ENTRY_URL_PROTOCOL.equals(protocol)) {
				return new org.eclipse.osgi.storage.url.bundleentry.Handler(modContainer, null);
			}
			else if (BundleResourceHandler.OSGI_RESOURCE_URL_PROTOCOL.equals(protocol)) {
				return new org.eclipse.osgi.storage.url.bundleresource.Handler(modContainer, null);
			}

			return null;
		}
	}
}
