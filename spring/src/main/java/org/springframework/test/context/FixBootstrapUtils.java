package org.springframework.test.context;

import org.springframework.test.context.cache.ContextCache;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import org.springframework.test.context.cache.DefaultContextCache;
import org.springframework.test.context.support.DefaultBootstrapContext;

public class FixBootstrapUtils extends BootstrapUtils {

    private static ThreadLocal<ContextCache> contextCache = new ThreadLocal<ContextCache>(){
        @Override
        protected ContextCache initialValue() {
            return new DefaultContextCache();
        }
    };

    public static BootstrapContext createBootstrapContext(Class<?> testClass) {
        CacheAwareContextLoaderDelegate contextLoader = new DefaultCacheAwareContextLoaderDelegate(contextCache.get());
        return new DefaultBootstrapContext(testClass, contextLoader);
    }

    public static TestContextBootstrapper resolveTestContextBootstrapper(BootstrapContext bootstrapContext) {
        return BootstrapUtils.resolveTestContextBootstrapper(bootstrapContext);
    }
}
