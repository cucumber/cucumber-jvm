package io.cucumber.java8;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;

import java.util.function.Supplier;

public final class Java8BackendProviderService implements BackendProviderService {

    private static final Logger log = LoggerFactory.getLogger(Java8BackendProviderService.class);

    @Override
    public Backend create(Lookup lookup, Container container, Supplier<ClassLoader> classLoaderProvider) {
        log.warn(() -> "" +
                "cucumber-java8 is deprecated. For details please see:\n" +
                " * https://github.com/cucumber/cucumber-jvm/issues/2174" +
                " * https://github.com/cucumber/cucumber-jvm/issues/1817" +
                " * https://github.com/jhalterman/typetools/issues/52");

        return new Java8Backend(lookup, container, classLoaderProvider);
    }

}
