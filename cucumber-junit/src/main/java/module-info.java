module io.cucumber.junit {
    requires transitive org.apiguardian.api;
    requires org.jspecify;

    requires io.cucumber.core;
    requires io.cucumber.core.gherkin;
    requires io.cucumber.plugin;
    requires io.cucumber.tagexpressions;

    requires junit;

    exports io.cucumber.junit;
}
