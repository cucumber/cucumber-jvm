package io.cucumber.picocontainer;

import org.apiguardian.api.API;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.injectors.Provider;
import org.picocontainer.injectors.ProviderAdapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to provide some additional PicoContainer
 * configuration. At the moment this covers:
 * <ul>
 * <li>a list of classes conforming the PicoContainer's {@link Provider}
 * interface,</li>
 * <li>a list of classes conforming the PicoContainer's {@link ProviderAdapter}
 * interface.</li>
 * </ul>
 * <p>
 * An example (ancillary containing the specific ProviderAdapter as nested
 * class) is:
 *
 * <pre>
 * package some.example;
 *
 * import java.sql.*;
 * import io.cucumber.picocontainer.PicoConfiguration;
 * import org.picocontainer.injectors.ProviderAdapter;
 *
 * &#64;PicoConfiguration(providerAdapters = { MyPicoConfiguration.DatabaseConnectionProvider.class })
 * public class MyPicoConfiguration {
 *
 *     public static class DatabaseConnectionProvider extends ProviderAdapter {
 *         public Connection provide() throws ClassNotFoundException, ReflectiveOperationException, SQLException {
 *             // Connecting to MySQL Using the JDBC DriverManager Interface
 *             // https://dev.mysql.com/doc/connector-j/en/connector-j-usagenotes-connect-drivermanager.html
 *             Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
 *             return DriverManager.getConnection("jdbc:mysql://localhost:3306/mydb", "mydbuser", "mydbpassword");
 *         }
 *     }
 *
 * }
 * </pre>
 * <p>
 * Notes:
 * <ul>
 * <li>Currently, there is no limitation to the number of
 * {@link PicoConfiguration} annotations. All of these annotations will be
 * considered when preparing the {@link org.picocontainer.PicoContainer
 * PicoContainer}.</li>
 * <li>If there is no {@link PicoConfiguration} annotation at all then (beside
 * the basic preparation) no additional PicoContainer preparation will be
 * done.</li>
 * <li>Cucumber PicoContainer uses PicoContainer's {@link MutablePicoContainer}
 * internally. Doing so, all {@link #providers() Providers} will be added by
 * {@link MutablePicoContainer#addAdapter(org.picocontainer.ComponentAdapter)
 * MutablePicoContainer#addAdapter(new ProviderAdapter(provider))} and all
 * {@link #providerAdapters() ProviderAdapters} will be added by
 * {@link MutablePicoContainer#addAdapter(org.picocontainer.ComponentAdapter)
 * MutablePicoContainer#addAdapter(adapter)}.</li>
 * <li>For each class there can be only one
 * {@link Provider}/{@link ProviderAdapter}. Otherwise an according exception
 * will be thrown (e.g. {@code PicoCompositionException} with message "Duplicate
 * Keys not allowed ..."</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@API(status = API.Status.EXPERIMENTAL)
public @interface PicoConfiguration {

    Class<? extends Provider>[] providers() default {};

    Class<? extends ProviderAdapter>[] providerAdapters() default {};

}
