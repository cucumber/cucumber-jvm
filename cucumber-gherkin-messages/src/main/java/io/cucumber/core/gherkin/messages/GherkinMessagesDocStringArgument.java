package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.DocStringArgument;
import io.cucumber.messages.types.PickleDocString;
import org.jspecify.annotations.Nullable;

final class GherkinMessagesDocStringArgument implements DocStringArgument {

    private final PickleDocString docString;
    private final int line;

    GherkinMessagesDocStringArgument(PickleDocString docString, int line) {
        this.docString = docString;
        this.line = line;
    }

    @Override
    public String getContent() {
        return docString.getContent();
    }

    @Override
    @Deprecated
    public @Nullable String getContentType() {
        return getMediaType();
    }

    @Override
    public @Nullable String getMediaType() {
        String mediaType = docString.getMediaType().orElse(null);
        if ("".equals(mediaType)) {
            return null;
        }
        return mediaType;
    }

    @Override
    public int getLine() {
        return line;
    }

}
