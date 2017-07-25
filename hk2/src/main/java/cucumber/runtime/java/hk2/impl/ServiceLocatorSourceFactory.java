package cucumber.runtime.java.hk2.impl;

import cucumber.runtime.Env;
import cucumber.runtime.java.hk2.ServiceLocatorSource;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

import static java.text.MessageFormat.format;

/**
 * Looks for a custom implementation of ServiceLocatorSource, or uses the default.
 */
public class ServiceLocatorSourceFactory {

    public static final String HK2_LOCATOR_SOURCE_KEY = "hk2.locator-source";

    private final Env env;

    public ServiceLocatorSourceFactory(Env env) {
        this.env = env;
    }

    public ServiceLocatorSource create() {

        String locatorSourceClassName = env.get(HK2_LOCATOR_SOURCE_KEY);

        if (locatorSourceClassName == null) {
            return createDefaultServiceLocatorSource();
        }

        return createUserSpecifiedServiceLocatorSource(locatorSourceClassName);
    }

    private ServiceLocatorSource createDefaultServiceLocatorSource() {
        return new ServiceLocatorSource() {
            @Override
            public ServiceLocator getServiceLocator() {

                ServiceLocator serviceLocator = ServiceLocatorFactory.getInstance().find("cucumber-default");

                if (serviceLocator == null) {
                    serviceLocator = ServiceLocatorUtilities.createAndPopulateServiceLocator("cucumber-default");
                }

                return serviceLocator;
            }
        };
    }

    private ServiceLocatorSource createUserSpecifiedServiceLocatorSource(String serviceLocatorSourceClassName) {
        try {
            return (ServiceLocatorSource) Class.forName(serviceLocatorSourceClassName).newInstance();
        } catch (Exception e) {
            String message = format("Instantiation of ''{0}'' failed. Check the caused by exception and ensure your" +
                            "ServiceLocatorSource implementation is accessible and has a public zero args constructor.",
                    serviceLocatorSourceClassName);
            throw new CucumberHK2Exception(message, e);
        }
    }
}
