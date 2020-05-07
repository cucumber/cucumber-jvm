package io.cucumber.guice;

import com.google.inject.Injector;
import org.apiguardian.api.API;

/**
 * An implementation of this interface is used to obtain an
 * <code>com.google.inject.Injector</code> that is used to provide instances of
 * all the classes that are used to run the Cucumber tests. The injector should
 * be configured with a binding for <code>ScenarioScope</code>.
 */
@API(status = API.Status.STABLE)
public interface InjectorSource {

    Injector getInjector();

}
