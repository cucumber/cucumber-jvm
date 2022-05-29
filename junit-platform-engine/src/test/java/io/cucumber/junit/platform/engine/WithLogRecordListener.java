package io.cucumber.core.logging;

import io.cucumber.core.logging.LogRecordListener;
import io.cucumber.core.logging.LoggerFactory;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith({ WithLogRecordListener.Extension.class })
public @interface WithLogRecordListener {
    class Extension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {
        private ExtensionContext.Store getContextStore(ExtensionContext context) {
            Optional<Class<?>> testClass = context.getTestClass();
            Object namespaceId = ExtensionContext.Namespace.GLOBAL;

            // Use test class name as namespace for the context store
            // If the test class is not available, fall back to the GLOBAL
            // namespace
            if (testClass.isPresent()) {
                namespaceId = testClass.get().getSimpleName();
            }

            ExtensionContext.Namespace namespace = ExtensionContext.Namespace.create(namespaceId);
            ExtensionContext.Store store = context.getStore(namespace);

            return store;
        }

        @Override
        public void beforeEach(ExtensionContext context) throws Exception {
            LogRecordListener logRecordListener = new LogRecordListener();
            LoggerFactory.addListener(logRecordListener);

            // Store the log record listener instance into a namespaced store
            // to prevent issues when running tests in parallel
            ExtensionContext.Store store = getContextStore(context);
            store.put("logRecordListener", logRecordListener);
        }

        @Override
        public void afterEach(ExtensionContext context) throws Exception {
            ExtensionContext.Store store = getContextStore(context);
            LoggerFactory.removeListener((LogRecordListener) store.get("logRecordListener"));
            store.remove("logRecordListener");
        }

        @Override
        public boolean supportsParameter(ParameterContext paramContext, ExtensionContext extContext)
                throws ParameterResolutionException {
            return paramContext.getParameter().getType() == LogRecordListener.class
                    && extContext.getTestMethod().isPresent();
        }

        @Override
        public Object resolveParameter(ParameterContext paramContext, ExtensionContext extContext)
                throws ParameterResolutionException {
            ExtensionContext.Store store = getContextStore(extContext);
            return store.get("logRecordListener");
        }
    }
}
