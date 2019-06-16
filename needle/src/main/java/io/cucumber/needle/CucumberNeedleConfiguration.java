package io.cucumber.needle;

import de.akquinet.jbosscc.needle.injection.InjectionProvider;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Reads cucumber-needle.properties to initialize additional {@link InjectionProvider}s.
 */
class CucumberNeedleConfiguration {
    /**
     * Default properties fiel name.
     */
    static final String RESOURCE_CUCUMBER_NEEDLE = "cucumber-needle";

    private final Set<InjectionProvider<?>> injectionProviders = new HashSet<>();

    /**
     * Creates new instance from default resource {@link #RESOURCE_CUCUMBER_NEEDLE}.
     */
    CucumberNeedleConfiguration() {
        this(RESOURCE_CUCUMBER_NEEDLE);
    }

    CucumberNeedleConfiguration(final String resourceName) {
        final ResourceBundle resourceBundle = LoadResourceBundle.INSTANCE.apply(resourceName);
        final Set<String> classNames = ReadInjectionProviderClassNames.INSTANCE.apply(resourceBundle);

        for (final String className : classNames) {
            try {
                final Class<?> clazz = Class.forName(className);
                CreateInstanceByDefaultConstructor createInstance = CreateInstanceByDefaultConstructor.INSTANCE;
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

    InjectionProvider<?>[] getInjectionProviders() {
        return injectionProviders.toArray(new InjectionProvider<?>[0]);
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
