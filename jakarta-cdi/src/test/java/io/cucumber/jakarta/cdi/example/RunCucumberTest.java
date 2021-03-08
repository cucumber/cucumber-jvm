package io.cucumber.jakarta.cdi.example;

import io.cucumber.jakarta.cdi.IgnoreLocalBeansXmlClassLoader;
import io.cucumber.junit.platform.engine.Cucumber;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestSourceRead;

@Cucumber
public class RunCucumberTest implements ConcurrentEventListener {

    private static final String WITHOUT_LOCAL_BEANS_XML = "withoutLocalBeansXml";

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {

        eventPublisher.registerHandlerFor(TestSourceRead.class, event -> {
            if (event.getUri().toString().contains(WITHOUT_LOCAL_BEANS_XML)) {
                IgnoreLocalBeansXmlClassLoader.setClassLoader(true);
            } else {
                IgnoreLocalBeansXmlClassLoader.restoreClassLoader();
            }
        });

        eventPublisher.registerHandlerFor(TestRunFinished.class, event -> {
            IgnoreLocalBeansXmlClassLoader.restoreClassLoader();
        });

    }

}
