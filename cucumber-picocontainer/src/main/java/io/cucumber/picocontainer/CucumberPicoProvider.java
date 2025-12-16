package io.cucumber.picocontainer;

import org.apiguardian.api.API;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.injectors.Provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to provide some additional PicoContainer
 * {@link Provider} classes.
 * <p>
 * An example is:
 *
 * <pre>
 * package some.example;
 *
 * import java.sql.*;
 * import io.cucumber.picocontainer.CucumberPicoProvider;
 * import org.picocontainer.injectors.Provider;
 *
 * &#64;CucumberPicoProvider
 * public class DatabaseConnectionProvider implements Provider {
 *     public Connection provide() throws ClassNotFoundException, ReflectiveOperationException, SQLException {
 *         // Connecting to MySQL Using the JDBC DriverManager Interface
 *         // https://dev.mysql.com/doc/connector-j/en/connector-j-usagenotes-connect-drivermanager.html
 *         Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
 *         return DriverManager.getConnection("jdbc:mysql://localhost:3306/mydb", "mydbuser", "mydbpassword");
 *     }
 * }
 * </pre>
 * <p>
 * In order to re-use existing {@link Provider}s, you can refer to those like
 * this:
 *
 * <pre>
 * package some.example;
 *
 * import io.cucumber.picocontainer.CucumberPicoProvider;
 * import some.other.namespace.SomeExistingProvider.class;
 *
 * &#64;CucumberPicoProvider(providers = { SomeExistingProvider.class })
 * public class MyCucumberPicoProviders {
 * }
 * </pre>
 * <p>
 * Notes:
 * <ul>
 * <li>Currently, there is no limitation to the number of
 * {@link CucumberPicoProvider} annotations. All of these annotations will be
 * considered when preparing the {@link org.picocontainer.PicoContainer
 * PicoContainer}.</li>
 * <li>If there is no {@link CucumberPicoProvider} annotation at all then
 * (beside the basic preparation) no additional PicoContainer preparation will
 * be done.</li>
 * <li>Cucumber PicoContainer uses PicoContainer's {@link MutablePicoContainer}
 * internally. Doing so, all {@link #providers() Providers} will be added by
 * {@link MutablePicoContainer#addAdapter(org.picocontainer.ComponentAdapter)
 * MutablePicoContainer#addAdapter(new ProviderAdapter(provider))}. (If any of
 * the providers additionally extends
 * {@link org.picocontainer.injectors.ProviderAdapter ProviderAdapter} then
 * these will be added directly without being wrapped again.)</li>
 * <li>For each class there can be only one {@link Provider}. Otherwise an
 * according exception will be thrown (e.g. {@code PicoCompositionException}
 * with message "Duplicate Keys not allowed ..."</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@API(status = API.Status.EXPERIMENTAL)
public @interface CucumberPicoProvider {

    Class<? extends Provider>[] providers() default {};

}
