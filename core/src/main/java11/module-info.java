module io.cucumber.core {
    exports io.cucumber.core.api;
    exports io.cucumber.core.backend;
    exports io.cucumber.core.cli;
    exports io.cucumber.core.logging;
    exports io.cucumber.core.event;
    exports io.cucumber.core.eventbus;
    exports io.cucumber.core.exception;
    exports io.cucumber.core.feature;
    exports io.cucumber.core.filter;
    exports io.cucumber.core.io;
    exports io.cucumber.core.options;
    exports io.cucumber.core.plugin;
    exports io.cucumber.core.reflection;
    exports io.cucumber.core.runner;
    exports io.cucumber.core.runtime;
    exports io.cucumber.core.snippets;
    exports io.cucumber.core.stepexpression;
    requires gherkin; // FIXME filename-based automodules detected
    requires io.cucumber.cucumberexpressions;
    requires io.cucumber.datatable;
    requires io.cucumber.tag.expressions;
    requires java.logging;
    requires java.xml;
    requires org.apiguardian.api;
}
