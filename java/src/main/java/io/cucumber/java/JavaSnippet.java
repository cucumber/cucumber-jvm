package io.cucumber.java;

import java.text.MessageFormat;

final class JavaSnippet extends AbstractJavaSnippet {

    @Override
    public MessageFormat template() {
        return new MessageFormat("" +
                "@{0}(\"{1}\")\n" +
                "public void {2}({3}) '{'\n" +
                "    // {4}\n" +
                "{5}    throw new " + PendingException.class.getName() + "();\n" +
                "'}'");
    }

}
