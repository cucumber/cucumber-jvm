package cucumber.runtime.java.hk2.integration;

import cucumber.runtime.java.hk2.ServiceLocatorSource;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

/**
 * Test service locator source
 */
public class TestServiceLocatorSource implements ServiceLocatorSource {

    @Override
    public ServiceLocator getServiceLocator() {

        ServiceLocator serviceLocator = ServiceLocatorFactory.getInstance().find("cucumber-test");

        if (serviceLocator == null) {
            serviceLocator = ServiceLocatorFactory.getInstance().create("cucumber-test");
            ServiceLocatorUtilities.bind(serviceLocator,
                    new TestBinder());
        }

        return serviceLocator;
    }
}
