package io.cucumber.docstring;

import org.apiguardian.api.API;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

@API(status = API.Status.EXPERIMENTAL)
public final class DocStringFormatter {

    private final String indentation;

    private DocStringFormatter(String indentation) {
        this.indentation = indentation;
    }

    public static DocStringFormatter.Builder builder() {
        return new Builder();
    }

    public String format(DocString docString) {
        StringBuilder result = new StringBuilder();
        formatTo(docString, result);
        return result.toString();
    }

    public void formatTo(DocString docString, StringBuilder appendable) {
        requireNonNull(docString, "docString may not be null");
        requireNonNull(appendable, "appendable may not be null");
        try {
            formatTo(docString, (Appendable) appendable);
        } catch (IOException e) {
            throw new CucumberDocStringException(e.getMessage(), e);
        }
    }

    public void formatTo(DocString docString, Appendable out) throws IOException {
        String printableContentType = docString.getContentType() == null ? "" : docString.getContentType();
        out.append(indentation).append("\"\"\"").append(printableContentType).append("\n");
        for (String l : docString.getContent().split("\\n")) {
            out.append(indentation).append(l).append("\n");
        }
        out.append(indentation).append("\"\"\"").append("\n");
    }

    public static final class Builder {

        private String indentation = "";

        private Builder() {

        }

        public Builder indentation(String indentation) {
            requireNonNull(indentation, "indentation may not be null");
            this.indentation = indentation;
            return this;
        }

        public DocStringFormatter build() {
            return new DocStringFormatter(indentation);
        }
    }
}
