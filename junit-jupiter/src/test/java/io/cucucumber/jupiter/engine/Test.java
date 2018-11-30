package io.cucucumber.jupiter.engine;


import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.reporting.ReportEntry;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Test {

    @org.junit.jupiter.api.Test
    public void main() throws URISyntaxException {
        CucumberTestEngine cucumberTestEngine = new CucumberTestEngine();
        EngineDiscoveryRequest discovery = new EngineDiscoveryRequest() {

            private Map<Class<?>, List<DiscoverySelector>> selectors = new HashMap<>();
            {
                selectors.put(ClasspathResourceSelector.class, Collections.singletonList(DiscoverySelectors.selectClasspathResource("io/cucumber/jupiter/engine")));
            }

            @Override
            public <T extends DiscoverySelector> List<T> getSelectorsByType(Class<T> selectorType) {
                return (List<T>) selectors.getOrDefault(selectorType, Collections.emptyList());
            }

            @Override
            public <T extends DiscoveryFilter<?>> List<T> getFiltersByType(Class<T> filterType) {
                return Collections.emptyList();
            }

            @Override
            public ConfigurationParameters getConfigurationParameters() {
                return null;
            }
        };
        UniqueId id = UniqueId.forEngine(cucumberTestEngine.getId());
        TestDescriptor discover = cucumberTestEngine.discover(discovery, id);

        EngineExecutionListener listenr = new EngineExecutionListener() {
            @Override
            public void dynamicTestRegistered(TestDescriptor testDescriptor) {
                System.out.println("test reg"  + testDescriptor);
            }

            @Override
            public void executionSkipped(TestDescriptor testDescriptor, String reason) {
                System.out.println("test skip"  + testDescriptor);

            }

            @Override
            public void executionStarted(TestDescriptor testDescriptor) {
                System.out.println("test start"  + testDescriptor);
            }

            @Override
            public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
                System.out.println("test finish"  + testDescriptor);

            }

            @Override
            public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
                System.out.println("test publish"  + testDescriptor);
            }
        };
        ConfigurationParameters conifg = new ConfigurationParameters() {
            @Override
            public Optional<String> get(String key) {
                return Optional.empty();
            }

            @Override
            public Optional<Boolean> getBoolean(String key) {
                return Optional.empty();
            }

            @Override
            public int size() {
                return 0;
            }
        };
        ExecutionRequest execution = new ExecutionRequest(discover, listenr, conifg);
        cucumberTestEngine.execute(execution);


    }
}
