package io.cucumber.core.logging;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.create;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith({ WithLogRecordListener.Extension.class })
public @interface WithLogRecordListener {
    class Extension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {
        private ExtensionContext.Store getContextStore(ExtensionContext context) {
            Namespace namespace = create(Extension.class, context.getRequiredTestMethod());
            return context.getStore(namespace);
        }

        private LogRecordListener getLogRecordListener(ExtensionContext context) {
            return getContextStore(context).getOrComputeIfAbsent(LogRecordListener.class);
        }

        @Override
        public void beforeEach(ExtensionContext extensionContext) {
            LogRecordListener listener = getLogRecordListener(extensionContext);
            LoggerFactory.addListener(listener);
        }

        @Override
        public void afterEach(ExtensionContext extensionContext) {
            LogRecordListener listener = getLogRecordListener(extensionContext);
            LoggerFactory.removeListener(listener);
        }

        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
                throws ParameterResolutionException {
            // @formatter:off
            return parameterContext.getParameter().getType() == LogRecordListener.class
                    && extensionContext.getTestMethod().isPresent();
            // @formatter:on
        }

        @Override
        public Object resolveParameter(ParameterContext paramContext, ExtensionContext context)
                throws ParameterResolutionException {
            return getLogRecordListener(context);
        }

    }

}
