package cucumber.runtime.java.guice;

import com.google.inject.Injector;

/**
 * An implentation of this interface is used to obtain an <code>com.google.inject.Injector</code> that is used to
 * provide instances of all the classes that are used to run the Cucumber tests. The injector should be configured with
 * a binding for <code>cucumber.runtime.java.guice.ScenarioScope</code>.
 */
public interface InjectorSource {
    Injector getInjector();
}
