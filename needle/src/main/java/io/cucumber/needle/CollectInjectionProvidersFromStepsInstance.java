package io.cucumber.needle;

import de.akquinet.jbosscc.needle.injection.InjectionProvider;
import de.akquinet.jbosscc.needle.reflection.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Collects {@link InjectionProvider} instances.
 */
enum CollectInjectionProvidersFromStepsInstance {
    /**
     * stateless Singleton
     */
    INSTANCE;

    /**
     * Logger for the factory.
     */
    private final Logger logger = LoggerFactory.getLogger(NeedleFactory.class);

    /**
     * Collect providers direct in the step definition.
     *
     * @param  instance step definition instance
     * @return          collected injection providers.
     */
    final <T> InjectionProvider<?>[] apply(final T instance) {
        final Set<InjectionProvider<?>> providers = new LinkedHashSet<>();
        for (final Field field : ReflectionUtil.getAllFieldsWithAnnotation(instance, NeedleInjectionProvider.class)) {
            field.setAccessible(true);
            try {
                final Object value = field.get(instance);
                if (value instanceof InjectionProvider<?>[]) {
                    providers.addAll(Arrays.asList((InjectionProvider<?>[]) value));
                } else if (value instanceof InjectionProvider) {
                    providers.add((InjectionProvider<?>) value);
                } else if (value instanceof InjectionProviderInstancesSupplier) {
                    providers.addAll(((InjectionProviderInstancesSupplier) value).get());
                } else {
                    throw new IllegalStateException("Fields annotated with NeedleInjectionProviders must be of type "
                            + "InjectionProviderInstancesSupplier, InjectionProvider " + "or InjectionProvider[]");
                }
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Adding {} InjectionProvider instances.", providers.size());
        }

        return providers.toArray(new InjectionProvider<?>[0]);
    }

}
