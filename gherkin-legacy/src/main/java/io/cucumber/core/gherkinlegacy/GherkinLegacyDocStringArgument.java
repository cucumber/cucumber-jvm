package io.cucumber.core.gherkinlegacy;

import gherkin.pickles.PickleString;
import io.cucumber.core.gherkin.DocStringArgument;

final class GherkinLegacyDocStringArgument implements DocStringArgument {

    private final PickleString docString;

    GherkinLegacyDocStringArgument(PickleString docString) {
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
