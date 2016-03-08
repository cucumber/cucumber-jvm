package cucumber.runtime.java.openwebbeans;

import cucumber.api.java.ObjectFactory;
import cucumber.api.openwebbeans.OpenWebBeansConfig;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.spi.ContainerLifecycle;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

public class OpenWebBeansObjectFactory implements ObjectFactory {
    private final Map<Class<?>, Object> notNormalScopedInstances = new HashMap<Class<?>, Object>();
    private final Collection<CreationalContext<?>> contexts = new ArrayList<CreationalContext<?>>();
    private WebBeansContext webBeansContext;
    private boolean managed;

    @Override
    public void start() {
        webBeansContext = WebBeansContext.currentInstance();
        final ContainerLifecycle cl = webBeansContext.getService(ContainerLifecycle.class);

        final Iterator<OpenWebBeansConfig> config = ServiceLoader.load(OpenWebBeansConfig.class).iterator();
        managed = !config.hasNext() || !config.next().userManaged();
        if (managed) {
            cl.startApplication(null);
        }
    }

    @Override
    public void stop() {
        synchronized (contexts) {
            for (final CreationalContext<?> cc : contexts) {
                cc.release();
            }
            contexts.clear();
            notNormalScopedInstances.clear();
        }
        if (managed) {
            webBeansContext.getService(ContainerLifecycle.class).stopApplication(null);
        }
    }

    @Override
    public boolean addClass(final Class<?> clazz) {
        return true;
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        final Object instance = notNormalScopedInstances.get(type);
        if (instance != null) {
            return type.cast(instance);
        }

        final BeanManagerImpl bm = webBeansContext.getBeanManagerImpl();
        final Bean<?> bean = bm.resolve(bm.getBeans(type));
        final CreationalContextImpl<Object> creationalContext = bm.createCreationalContext(null);
        T created = type.cast(bm.getReference(bean, type, creationalContext));
        if (!bm.isNormalScope(bean.getScope())) {
            synchronized (contexts) {
                contexts.add(creationalContext);
                final Object existing = notNormalScopedInstances.put(type, created);
                if (existing != null) {
                    created = type.cast(existing);
                    creationalContext.release();
                }
            }
        }
        return created;
    }
}

