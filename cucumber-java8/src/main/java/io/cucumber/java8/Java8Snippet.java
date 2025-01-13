package io.cucumber.java8;

import io.cucumber.core.gherkin.Step;

import java.text.MessageFormat;

final class Java8Snippet extends AbstractJavaSnippet {

    @Override
    public MessageFormat template(Step step) {
        return new MessageFormat("" +
                "{0}(\"{1}\", ({3}) -> '{'\n" +
                "    // {4}\n" +
                "{5}    throw new " + PendingException.class.getName() + "();\n" +
                "'}');");
    }

}
