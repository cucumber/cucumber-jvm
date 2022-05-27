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

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith({ WithLogRecordListener.Extension.class })
public @interface WithLogRecordListener {
    class Extension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {
        protected LogRecordListener logRecordListener;

        @Override
        public void beforeEach(ExtensionContext context) throws Exception {
            logRecordListener = new LogRecordListener();
            LoggerFactory.addListener(logRecordListener);
        }

        @Override
        public void afterEach(ExtensionContext context) throws Exception {
            LoggerFactory.removeListener(logRecordListener);
        }

        @Override
        public boolean supportsParameter(ParameterContext paramContext, ExtensionContext extContext)
                throws ParameterResolutionException {
            return paramContext.getParameter().getType() == LogRecordListener.class;
        }

        @Override
        public Object resolveParameter(ParameterContext paramContext, ExtensionContext extContext)
                throws ParameterResolutionException {
            return logRecordListener;
        }
    }
}
