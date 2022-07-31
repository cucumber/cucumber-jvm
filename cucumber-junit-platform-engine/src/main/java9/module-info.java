module io.cucumber.junit.platform.engine {
    requires io.cucumber.core;
    requires io.cucumber.core.gherkin;

    requires org.junit.platform.commons;

    requires transitive org.opentest4j;
    requires transitive org.apiguardian.api;
    requires transitive org.junit.platform.engine;

    exports io.cucumber.junit.platform.engine;
    provides org.junit.platform.engine.TestEngine with io.cucumber.junit.platform.engine.CucumberTestEngine;
}