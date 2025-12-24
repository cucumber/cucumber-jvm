module io.cucumber.core {
    requires com.fasterxml.jackson.databind; // TODO: Shading?
    requires com.fasterxml.jackson.datatype.jdk8;

    requires io.cucumber.cienvironment;
    requires io.cucumber.core.gherkin;
    requires io.cucumber.cucumberexpressions;
    requires io.cucumber.gherkin;
    requires io.cucumber.htmlformatter;
    requires io.cucumber.jsonformatter;
    requires io.cucumber.junitxmlformatter;
    requires io.cucumber.messages;
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

    requires transitive io.cucumber.datatable;
    requires transitive io.cucumber.docstring;
    requires transitive org.apiguardian.api;

    exports cucumber.api.cli;
    exports io.cucumber.core.backend;
    exports io.cucumber.core.cli;
    exports io.cucumber.core.plugin;
    exports io.cucumber.core.runner;
    exports io.cucumber.core.eventbus;

    uses io.cucumber.core.gherkin.FeatureParser;
    uses io.cucumber.core.backend.BackendProviderService;
    uses io.cucumber.core.backend.ObjectFactory;

    provides io.cucumber.core.backend.ObjectFactory with io.cucumber.core.backend.DefaultObjectFactory;
    provides io.cucumber.core.eventbus.UuidGenerator
            with io.cucumber.core.eventbus.RandomUuidGenerator, io.cucumber.core.eventbus.IncrementingUuidGenerator;

}
