package io.cucumber.core.plugin;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Object representation of <a href="https://github.com/cucumber/cucumber-json-schema/blob/main/schemas/cucumber-jvm.json">cucumber-jvm.json</a> schema.
 */
class CucumberJvmJson {
    enum JvmElementType {
        background, scenario
    }
    enum JvmStatus {
        passed,
        failed,
        skipped,
        undefined,
        pending
    }

    static class JvmFeature {
        private final String uri;
        private final String id;
        private final Long line;
        private final String keyword;
        private final String name;
        private final String description;
        private final List<JvmElement> elements;
        private final List<JvmLocationTag> tags;

        JvmFeature(String uri, String id, Long line, String keyword, String name, String description, List<JvmElement> elements, List<JvmLocationTag> tags) {
            this.uri = requireNonNull(uri);
            this.id = requireNonNull(id);
            this.line = requireNonNull(line);
            this.keyword = requireNonNull(keyword);
            this.name = requireNonNull(name);
            this.description = requireNonNull(description);
            this.elements = requireNonNull(elements);
            this.tags = tags;
        }

        public String getUri() {
            return uri;
        }

        public String getId() {
            return id;
        }

        public Long getLine() {
            return line;
        }

        public String getKeyword() {
            return keyword;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public List<JvmElement> getElements() {
            return elements;
        }

        public List<JvmLocationTag> getTags() {
            return tags;
        }
    }

    static class JvmElement {
        private final String start_timestamp;
        private final Long line;
        private final String id;
        private final JvmElementType type;
        private final String keyword;
        private final String name;
        private final String description;
        private final List<JvmStep> steps;
        private final List<JvmHook> before;
        private final List<JvmHook> after;
        private final List<JvmTag> tags;

        JvmElement(String start_timestamp, Long line, String id, JvmElementType type, String keyword, String name, String description, List<JvmStep> steps, List<JvmHook> before, List<JvmHook> after, List<JvmTag> tags) {
            this.start_timestamp = start_timestamp;
            this.line = requireNonNull(line);
            this.id = id;
            this.type = requireNonNull(type);
            this.keyword = requireNonNull(keyword);
            this.name = requireNonNull(name);
            this.description = requireNonNull(description);
            this.steps = requireNonNull(steps);
            this.before = before;
            this.after = after;
            this.tags = tags;
        }

        public String getStart_timestamp() {
            return start_timestamp;
        }

        public Long getLine() {
            return line;
        }

        public String getId() {
            return id;
        }

        public JvmElementType getType() {
            return type;
        }

        public String getKeyword() {
            return keyword;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public List<JvmStep> getSteps() {
            return steps;
        }

        public List<JvmHook> getBefore() {
            return before;
        }

        public List<JvmHook> getAfter() {
            return after;
        }

        public List<JvmTag> getTags() {
            return tags;
        }
    }

    static class JvmStep {
        private final String keyword;
        private final Long line;
        private final JvmMatch match;
        private final String name;
        private final JvmResult result;
        private final JvmDocString doc_string;
        private final List<JvmDataTableRow> rows;

        JvmStep(String keyword, Long line, JvmMatch match, String name, JvmResult result, JvmDocString doc_string, List<JvmDataTableRow> rows) {
            this.keyword = requireNonNull(keyword);
            this.line = requireNonNull(line);
            this.match = match;
            this.name = requireNonNull(name);
            this.result = requireNonNull(result);
            this.doc_string = doc_string;
            this.rows = rows;
        }

        public String getKeyword() {
            return keyword;
        }

        public Long getLine() {
            return line;
        }

        public JvmMatch getMatch() {
            return match;
        }

        public String getName() {
            return name;
        }

        public JvmResult getResult() {
            return result;
        }

        public JvmDocString getDoc_string() {
            return doc_string;
        }

        public List<JvmDataTableRow> getRows() {
            return rows;
        }
    }

    static class JvmMatch {
        private final String location;
        private final List<JvmArgument> arguments;

        JvmMatch(String location, List<JvmArgument> arguments) {
            this.location = location;
            this.arguments = arguments;
        }

        public String getLocation() {
            return location;
        }

        public List<JvmArgument> getArguments() {
            return arguments;
        }
    }

    static class JvmArgument {
        private final String val;
        private final Number offset;

        JvmArgument(String val, Number offset) {
            this.val = requireNonNull(val);
            this.offset = requireNonNull(offset);
        }

        public String getVal() {
            return val;
        }

        public Number getOffset() {
            return offset;
        }
    }

    static class JvmResult {
        private final Long duration;
        private final JvmStatus status;
        private final String error_message;

        JvmResult(Long duration, JvmStatus status, String error_message) {
            this.duration = duration;
            this.status = requireNonNull(status);
            this.error_message = error_message;
        }

        public Long getDuration() {
            return duration;
        }

        public JvmStatus getStatus() {
            return status;
        }

        public String getError_message() {
            return error_message;
        }
    }

    static class JvmDocString {
        private final Long line;
        private final String value;
        private final String content_type;

        JvmDocString(Long line, String value, String content_type) {
            this.line = requireNonNull(line);
            this.value = requireNonNull(value);
            this.content_type = content_type;
        }

        public Long getLine() {
            return line;
        }

        public String getValue() {
            return value;
        }

        public String getContent_type() {
            return content_type;
        }
    }

    static class JvmDataTableRow {
        private final List<String> cells;

        JvmDataTableRow(List<String> cells) {
            this.cells = requireNonNull(cells);
        }

        public List<String> getCells() {
            return cells;
        }
    }

    static class JvmHook {
        private final JvmMatch match;
        private final JvmResult result;

        JvmHook(JvmMatch match, JvmResult result) {
            this.match = requireNonNull(match);
            this.result = requireNonNull(result);
        }

        public JvmMatch getMatch() {
            return match;
        }

        public JvmResult getResult() {
            return result;
        }
    }

    static class JvmTag {
        private final String name;

        JvmTag(String name) {
            this.name = requireNonNull(name);
        }

        public String getName() {
            return name;
        }
    }

    static class JvmLocationTag {
        private final String name;
        private final String type;
        private final JvmLocation location;

        JvmLocationTag(String name, String type, JvmLocation location) {
            this.name = requireNonNull(name);
            this.type = requireNonNull(type);
            this.location = requireNonNull(location);
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public JvmLocation getLocation() {
            return location;
        }
    }

    static class JvmLocation {
        private final Long line;
        private final Long column;

        JvmLocation(Long line, Long column) {
            this.line = requireNonNull(line);
            this.column = requireNonNull(column);
        }

        public Long getLine() {
            return line;
        }

        public Long getColumn() {
            return column;
        }
    }
}
