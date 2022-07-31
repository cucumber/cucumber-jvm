package io.cucumber.java8;

import java.text.MessageFormat;

final class Java8Snippet extends AbstractJavaSnippet {

    @Override
    public MessageFormat template() {
        return new MessageFormat("" +
                "{0}(\"{1}\", ({3}) -> '{'\n" +
                "    // {4}\n" +
                "{5}    throw new " + PendingException.class.getName() + "();\n" +
                "'}');");
    }

}
