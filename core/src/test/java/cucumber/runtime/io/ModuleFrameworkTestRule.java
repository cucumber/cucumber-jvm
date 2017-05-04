package cucumber.runtime.io;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.URL;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.rules.ExternalResource;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * @author mdelapenya
 */
public class ModuleFrameworkTestRule extends ExternalResource {

	public Bundle getTestBundle() {
		return testBundle;
	}

	@Override
	protected void before() throws Throwable {
		felixModuleFramework = startOSGi();

		if (felixModuleFramework == null) {
			throw new RuntimeException(
				"Module framework is not ready.");
		}

		ClassLoader classLoader = getClass().getClassLoader();

		BundleContext bundleContext =
			felixModuleFramework.getBundleContext();

		URL bundleURL = classLoader.getResource(
			"cucumber/runtime/http-servlet.jar");

		testBundle = bundleContext.installBundle(bundleURL.toString());

		System.out.println(
			"Bundle " + testBundle.getLocation() + " has been installed.");
	}

	@Override
	protected void after() {
		try {
			stopOSGi(felixModuleFramework, testBundle);
		}
		catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	private static FrameworkFactory getFrameworkFactory() throws Exception {
		java.net.URL url =
			ModuleFrameworkTestRule.class.getClassLoader().getResource(
				"META-INF/services/org.osgi.framework.launch.FrameworkFactory");

		if (url != null) {
			BufferedReader br = new BufferedReader(
				new InputStreamReader(url.openStream()));

			try {
				for (String s = br.readLine(); s != null; s = br.readLine()) {
					s = s.trim();

					// Try to load first non-empty, non-commented line.

					if ((s.length() > 0) && (s.charAt(0) != '#')) {
						return
							(FrameworkFactory) Class.forName(s).newInstance();
					}
				}
			}
			finally {
				if (br != null) {
					br.close();
				}
			}
		}

		throw new Exception("Could not find framework factory.");
	}

	private static Framework startOSGi() throws Exception {
		Map<String, String> map = new HashMap<String, String>();

		Properties configProps = new Properties();

		configProps.put(
			Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
			"cucumber.runtime.tests; version=1.0.0");

		FrameworkFactory factory = getFrameworkFactory();

		final Framework framework = factory.newFramework(map);

		Runtime.getRuntime().addShutdownHook(
			new Thread("Felix Shutdown Hook") {

				public void run() {
					try {
						if (framework != null) {
							framework.stop();

							System.out.println("Stopping OSGi from shutdown hook");

							framework.waitForStop(0);
						}
					}
					catch (Exception ex) {
						System.err.println("Error stopping framework: " + ex);
					}
				}
			});

		System.out.println("Starting OSGI...");

		framework.start();

		return framework;
	}

	private static void stopOSGi(Framework framework, Bundle... bundles)
		throws BundleException, InterruptedException {

		for (Bundle bundle : bundles) {
			bundle.uninstall();
		}

		framework.stop();

		framework.waitForStop(10);

		System.out.println("Stopping OSGI");
	}

	private Framework felixModuleFramework;
	private Bundle testBundle;

}