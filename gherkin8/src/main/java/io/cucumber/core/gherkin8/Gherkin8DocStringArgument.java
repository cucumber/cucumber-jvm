package io.cucumber.core.gherkin8;

import io.cucumber.core.gherkin.DocStringArgument;
import io.cucumber.messages.Messages.PickleStepArgument.PickleDocString;

public final class Gherkin8DocStringArgument implements DocStringArgument {

    private final PickleDocString docString;

    Gherkin8DocStringArgument(PickleDocString docString) {
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
        throw new UnsupportedOperationException("Not supported");
    }
}
