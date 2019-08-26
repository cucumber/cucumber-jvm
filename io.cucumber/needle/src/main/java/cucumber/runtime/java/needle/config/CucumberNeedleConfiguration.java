package cucumber.runtime.java.needle.config;

import cucumber.api.needle.InjectionProviderInstancesSupplier;
import de.akquinet.jbosscc.needle.injection.InjectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Reads cucumber-needle.properties to initialize additional {@link InjectionProvider}s.
 */
public class CucumberNeedleConfiguration {
    /**
     * Default properties fiel name.
     */
    public static final String RESOURCE_CUCUMBER_NEEDLE = "cucumber-needle";

    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LoadResourceBundle loadResourceBundle = LoadResourceBundle.INSTANCE;
    private final ReadInjectionProviderClassNames readInjectionProviderClassNames = ReadInjectionProviderClassNames.INSTANCE;
    private final CreateInstanceByDefaultConstructor createInstance = CreateInstanceByDefaultConstructor.INSTANCE;

    private final Set<InjectionProvider<?>> injectionProviders = new HashSet<InjectionProvider<?>>();

    /**
     * Creates new instance from default resource {@link #RESOURCE_CUCUMBER_NEEDLE}.
     */
    public CucumberNeedleConfiguration() {
        this(RESOURCE_CUCUMBER_NEEDLE);
    }

    public CucumberNeedleConfiguration(final String resourceName) {
        final ResourceBundle resourceBundle = loadResourceBundle.apply(resourceName);
        final Set<String> classNames = readInjectionProviderClassNames.apply(resourceBundle);

        for (final String className : classNames) {
            try {
                final Class<?> clazz = Class.forName(className);
                if (isInjectionProvider(clazz)) {
                    injectionProviders.add((InjectionProvider<?>) createInstance.apply(clazz));
                } else if (isInjectionProviderInstanceSupplier(clazz)) {
                    final InjectionProviderInstancesSupplier supplier = (InjectionProviderInstancesSupplier) createInstance
                            .apply(clazz);
                    final Set<InjectionProvider<?>> providers = supplier.get();
                    if (providers != null) {
                        injectionProviders.addAll(providers);
                    }
                }
            } catch (final Exception e) {
                throw new IllegalStateException("failed to initialize custom injection providers", e);
            }
        }
    }

    public InjectionProvider<?>[] getInjectionProviders() {
        return injectionProviders.toArray(new InjectionProvider<?>[injectionProviders.size()]);
    }

    /**
     * Checks if given class is an {@link InjectionProvider}
     *
     * @param type Class to check
     * @return <code>true</code> if type can be cast to {@link InjectionProvider}
     */
    static boolean isInjectionProvider(final Class<?> type) {
        return InjectionProvider.class.isAssignableFrom(type);
    }

    /**
     * Checks if given class is an {@link InjectionProviderInstancesSupplier}
     *
     * @param type Class to check
     * @return <code>true</code> if type can be cast to {@link InjectionProviderInstancesSupplier}
     */
    static boolean isInjectionProviderInstanceSupplier(final Class<?> type) {
        return InjectionProviderInstancesSupplier.class.isAssignableFrom(type);
    }
}
