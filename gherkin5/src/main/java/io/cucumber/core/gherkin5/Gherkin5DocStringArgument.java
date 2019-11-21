package io.cucumber.core.gherkin5;

import gherkin.pickles.PickleString;
import io.cucumber.core.gherkin.DocStringArgument;

final class Gherkin5DocStringArgument implements DocStringArgument {

    private final PickleString docString;

    Gherkin5DocStringArgument(PickleString docString) {
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
