module io.cucumber.core {
    requires com.fasterxml.jackson.databind; // TODO: Shading?
    requires com.fasterxml.jackson.datatype.jdk8;

    requires io.cucumber.cienvironment;
    requires io.cucumber.core.gherkin;
    requires io.cucumber.gherkin;
    requires io.cucumber.htmlformatter;
    requires io.cucumber.jsonformatter;
    requires io.cucumber.junitxmlformatter;
    requires io.cucumber.plugin;
    requires io.cucumber.prettyformatter;
    requires io.cucumber.query;
    requires io.cucumber.tagexpressions;
    requires io.cucumber.teamcityformatter;
    requires io.cucumber.testngxmlformatter;
    requires io.cucumber.usageformatter;
    requires java.compiler;
    requires java.logging;
    requires org.jspecify;

    requires transitive io.cucumber.messages;
    requires transitive io.cucumber.datatable;
    requires transitive io.cucumber.docstring;
    requires transitive io.cucumber.cucumberexpressions;
    requires transitive org.apiguardian.api;

    exports cucumber.api.cli;
    exports io.cucumber.core.backend;
    exports io.cucumber.core.cli;
    exports io.cucumber.core.plugin;
    exports io.cucumber.core.runner;
    exports io.cucumber.core.eventbus;
    exports io.cucumber.core.runtime;
    exports io.cucumber.core.feature;
    exports io.cucumber.core.resource;
    exports io.cucumber.core.logging;
    exports io.cucumber.core.options;
    exports io.cucumber.core.snippets;
    exports io.cucumber.core.exception;
    exports io.cucumber.core.filter;

    uses io.cucumber.core.gherkin.FeatureParser;
    uses io.cucumber.core.backend.BackendProviderService;
    uses io.cucumber.core.backend.ObjectFactory;
    uses io.cucumber.core.eventbus.UuidGenerator;

    provides io.cucumber.core.backend.ObjectFactory with io.cucumber.core.backend.DefaultObjectFactory;
    provides io.cucumber.core.eventbus.UuidGenerator
            with io.cucumber.core.eventbus.RandomUuidGenerator, io.cucumber.core.eventbus.IncrementingUuidGenerator;

}
