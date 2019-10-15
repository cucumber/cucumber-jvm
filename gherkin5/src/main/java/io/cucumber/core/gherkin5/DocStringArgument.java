package io.cucumber.core.gherkin5;

import gherkin.pickles.PickleString;
import io.cucumber.core.gherkin.Argument;

public final class DocStringArgument implements Argument, io.cucumber.plugin.event.DocStringArgument {

    private final PickleString docString;

    DocStringArgument(PickleString docString) {
        this.docString = docString;
    }

    @Override
    public String getContent() {
        return docString.getContent();
    }

    @Override
    public String getContentType() {
        return docString.getContentType();
    }

    @Override
    public int getLine() {
        return docString.getLocation().getLine();
    }
}
