module io.cucumber.junit.platform.engine {
    requires io.cucumber.core;

    requires org.opentest4j;
    requires org.junit.platform.commons;

    requires transitive org.apiguardian.api;
    requires transitive org.junit.platform.engine;

    exports io.cucumber.junit.platform.engine;
    provides org.junit.platform.engine.TestEngine with io.cucumber.junit.platform.engine.CucumberTestEngine;
}