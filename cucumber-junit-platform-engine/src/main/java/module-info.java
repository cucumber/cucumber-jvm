module io.cucumber.junit.platform.engine {
    requires org.jspecify;
    requires transitive org.apiguardian.api;

    requires org.junit.platform.engine;

    requires io.cucumber.core;
    requires io.cucumber.plugin;
    requires io.cucumber.core.gherkin;
    requires io.cucumber.tagexpressions;

    exports io.cucumber.junit.platform.engine;

    provides org.junit.platform.engine.TestEngine with io.cucumber.junit.platform.engine.CucumberTestEngine;
}
