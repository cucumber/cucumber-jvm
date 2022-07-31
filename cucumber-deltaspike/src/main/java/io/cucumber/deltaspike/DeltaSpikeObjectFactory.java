package io.cucumber.deltaspike;

import io.cucumber.core.backend.ObjectFactory;
import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apiguardian.api.API;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import java.util.Set;

@API(status = API.Status.STABLE)
public final class DeltaSpikeObjectFactory implements ObjectFactory {

    private final CdiContainer container;

    public DeltaSpikeObjectFactory() {
        this.container = CdiContainerLoader.getCdiContainer();
    }

    @Override
    public void start() {
        container.boot();
    }

    @Override
    public void stop() {
        container.shutdown();
    }

    @Override
    public boolean addClass(final Class<?> clazz) {
        return true;
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        final BeanManager beanManager = container.getBeanManager();
        final Set<Bean<?>> beans = beanManager.getBeans(type);
        final Bean<?> bean = beanManager.resolve(beans);
        final CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
        try {
            return type.cast(beanManager.getReference(bean, type, creationalContext));
        } finally {
            creationalContext.release();
        }
    }

}
