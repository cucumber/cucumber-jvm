module io.cucumber.core.gherkin.messages {
    requires org.jspecify;
    requires transitive org.apiguardian.api;

    requires io.cucumber.core.gherkin;
    requires io.cucumber.plugin;
    requires transitive io.cucumber.messages;
    requires io.cucumber.gherkin;

    exports io.cucumber.core.gherkin.messages;

    provides io.cucumber.core.gherkin.FeatureParser with io.cucumber.core.gherkin.messages.GherkinMessagesFeatureParser;

}
