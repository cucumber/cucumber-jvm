package io.cucumber.core.stepexpression;

public final class DocString {
    private final String text;
    private final String contentType;

    public String getText() {
        return text;
    }

    public String getContentType() {
        return contentType;
    }

    DocString(String text, String contentType) {
        this.text = text;
        this.contentType = contentType == null ? "" : contentType;
    }
}
