package io.cucumber.core.feature;

import gherkin.pickles.PickleString;

public final class DocStringArgument implements Argument, io.cucumber.core.event.DocStringArgument {

    private final PickleString docString;

    DocStringArgument(PickleString docString) {
        this.docString = docString;
    }

    public String getContent() {
        return docString.getContent();
    }

    public String getContentType() {
        return docString.getContentType();
    }

    @Override
    public int getLine() {
        return docString.getLocation().getLine();
    }
}
