package cucumber.runtime.java.hk2;

import org.glassfish.hk2.api.ServiceLocator;

/**
 * Provides the ServiceLocator source to use during Cucumber tests
 */
public interface ServiceLocatorSource {

    ServiceLocator getServiceLocator();
}
