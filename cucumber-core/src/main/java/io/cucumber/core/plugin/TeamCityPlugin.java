package io.cucumber.core.plugin;

import io.cucumber.messages.Convertor;
import io.cucumber.messages.types.Attachment;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Exception;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Hook;
import io.cucumber.messages.types.JavaMethod;
import io.cucumber.messages.types.JavaStackTraceElement;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.PickleStep;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.SourceReference;
import io.cucumber.messages.types.TableRow;
import io.cucumber.messages.types.TestCaseFinished;
import io.cucumber.messages.types.TestCaseStarted;
import io.cucumber.messages.types.TestRunStarted;
import io.cucumber.messages.types.TestStep;
import io.cucumber.messages.types.TestStepFinished;
import io.cucumber.messages.types.TestStepResult;
import io.cucumber.messages.types.TestStepResultStatus;
import io.cucumber.messages.types.TestStepStarted;
import io.cucumber.messages.types.Timestamp;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.SnippetsSuggestedEvent.Suggestion;
import io.cucumber.query.LineageReducer;
import io.cucumber.query.Query;

import java.io.Closeable;
import java.io.PrintStream;
import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.cucumber.messages.Convertor.toDuration;
import static io.cucumber.query.LineageReducer.descending;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * Outputs Teamcity services messages to std out.
 *
 * @see <a
 *      href=https://www.jetbrains.com/help/teamcity/service-messages.html>TeamCity
 *      - Service Messages</a>
 */
public class TeamCityPlugin implements EventListener {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss.SSSZ");

    private static final String TEAMCITY_PREFIX = "##teamcity";

    private static final String TEMPLATE_ENTER_THE_MATRIX = TEAMCITY_PREFIX + "[enteredTheMatrix timestamp = '%s']";
    private static final String TEMPLATE_TEST_RUN_STARTED = TEAMCITY_PREFIX
            + "[testSuiteStarted timestamp = '%s' name = 'Cucumber']";
    private static final String TEMPLATE_TEST_RUN_FINISHED = TEAMCITY_PREFIX
            + "[testSuiteFinished timestamp = '%s' name = 'Cucumber']";

    private static final String TEMPLATE_TEST_SUITE_STARTED = TEAMCITY_PREFIX
            + "[testSuiteStarted timestamp = '%s' locationHint = '%s' name = '%s']";
    private static final String TEMPLATE_TEST_SUITE_FINISHED = TEAMCITY_PREFIX
            + "[testSuiteFinished timestamp = '%s' name = '%s']";

    private static final String TEMPLATE_TEST_STARTED = TEAMCITY_PREFIX
            + "[testStarted timestamp = '%s' locationHint = '%s' captureStandardOutput = 'true' name = '%s']";
    private static final String TEMPLATE_TEST_FINISHED = TEAMCITY_PREFIX
            + "[testFinished timestamp = '%s' duration = '%s' name = '%s']";
    private static final String TEMPLATE_TEST_FAILED = TEAMCITY_PREFIX
            + "[testFailed timestamp = '%s' duration = '%s' message = '%s' details = '%s' name = '%s']";

    private static final String TEMPLATE_TEST_COMPARISON_FAILED = TEAMCITY_PREFIX
            + "[testFailed timestamp = '%s' duration = '%s' message = '%s' details = '%s' expected = '%s' actual = '%s' name = '%s']";
    private static final String TEMPLATE_TEST_IGNORED = TEAMCITY_PREFIX
            + "[testIgnored timestamp = '%s' duration = '%s' message = '%s' name = '%s']";

    private static final String TEMPLATE_BEFORE_ALL_AFTER_ALL_STARTED = TEAMCITY_PREFIX
            + "[testStarted timestamp = '%s' name = '%s']";
    private static final String TEMPLATE_BEFORE_ALL_AFTER_ALL_FAILED = TEAMCITY_PREFIX
            + "[testFailed timestamp = '%s' message = '%s' details = '%s' name = '%s']";
    private static final String TEMPLATE_BEFORE_ALL_AFTER_ALL_FINISHED = TEAMCITY_PREFIX
            + "[testFinished timestamp = '%s' name = '%s']";

    private static final String TEMPLATE_PROGRESS_COUNTING_STARTED = TEAMCITY_PREFIX
            + "[customProgressStatus testsCategory = 'Scenarios' count = '0' timestamp = '%s']";
    private static final String TEMPLATE_PROGRESS_COUNTING_FINISHED = TEAMCITY_PREFIX
            + "[customProgressStatus testsCategory = '' count = '0' timestamp = '%s']";
    private static final String TEMPLATE_PROGRESS_TEST_STARTED = TEAMCITY_PREFIX
            + "[customProgressStatus type = 'testStarted' timestamp = '%s']";
    private static final String TEMPLATE_PROGRESS_TEST_FINISHED = TEAMCITY_PREFIX
            + "[customProgressStatus type = 'testFinished' timestamp = '%s']";

    private static final String TEMPLATE_ATTACH_WRITE_EVENT = TEAMCITY_PREFIX + "[message text='%s' status='NORMAL']";

    private final LineageReducer<List<TreeNode>> pathCollector = descending(PathCollector::new);
    private final Query query = new Query();
    private final List<SnippetsSuggestedEvent> suggestions = new ArrayList<>();
    private final TeamCityCommandWriter out;

    private List<TreeNode> currentPath = new ArrayList<>();
    private Pickle currentPickle;

    @SuppressWarnings("unused") // Used by PluginFactory
    public TeamCityPlugin() {
        // This plugin prints markers for Team City and IntelliJ IDEA that
        // allows them to associate the output to specific test cases. Printing
        // to system out - and potentially mixing with other formatters - is
        // intentional.
        this(System.out);
    }

    TeamCityPlugin(PrintStream out) {
        this.out = new TeamCityCommandWriter(out);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(Envelope.class, event -> {
            query.update(event);
            event.getTestRunStarted().ifPresent(this::printTestRunStarted);
            event.getTestCaseStarted().ifPresent(this::printTestCaseStarted);
            event.getTestStepStarted().ifPresent(this::printTestStepStarted);
            event.getTestStepFinished().ifPresent(this::printTestStepFinished);
            event.getTestCaseFinished().ifPresent(this::printTestCaseFinished);
            event.getTestRunFinished().ifPresent(this::printTestRunFinished);
            event.getAttachment().ifPresent(this::handleEmbedEvent);
        });
        // TODO: Replace with messages
        publisher.registerHandlerFor(SnippetsSuggestedEvent.class, this::handleSnippetSuggested);
    }

    private void printTestRunStarted(TestRunStarted event) {
        String timestamp = formatTimeStamp(event.getTimestamp());
        out.print(TEMPLATE_ENTER_THE_MATRIX, timestamp);
        out.print(TEMPLATE_TEST_RUN_STARTED, timestamp);
        out.print(TEMPLATE_PROGRESS_COUNTING_STARTED, timestamp);
    }

    private void printTestCaseStarted(TestCaseStarted event) {
        query.findPickleBy(event)
                .ifPresent(pickle -> findPathTo(pickle)
                        .ifPresent(path -> {
                            String timestamp = formatTimeStamp(event.getTimestamp());
                            poppedNodes(path).forEach(node -> finishNode(timestamp, node));
                            pushedNodes(path).forEach(node -> startNode(timestamp, node));
                            this.currentPath = path;
                            this.currentPickle = pickle;
                            out.print(TEMPLATE_PROGRESS_TEST_STARTED, timestamp);
                        }));
    }

    private Optional<List<TreeNode>> findPathTo(Pickle pickle) {
        return query.findLineageBy(pickle)
                .map(lineage -> pathCollector.reduce(lineage, pickle));
    }

    private void startNode(String timestamp, TreeNode node) {
        String name = node.getName();
        String location = node.getUri() + ":" + node.getLocation().getLine();
        out.print(TEMPLATE_TEST_SUITE_STARTED, timestamp, location, name);
    }

    private void finishNode(String timestamp, TreeNode node) {
        String name = node.getName();
        out.print(TEMPLATE_TEST_SUITE_FINISHED, timestamp, name);
    }

    private List<TreeNode> poppedNodes(List<TreeNode> newStack) {
        List<TreeNode> nodes = new ArrayList<>(reversedPoppedNodes(currentPath, newStack));
        Collections.reverse(nodes);
        return nodes;
    }

    private List<TreeNode> reversedPoppedNodes(List<TreeNode> currentStack, List<TreeNode> newStack) {
        for (int i = 0; i < currentStack.size() && i < newStack.size(); i++) {
            if (!currentStack.get(i).equals(newStack.get(i))) {
                return currentStack.subList(i, currentStack.size());
            }
        }
        if (newStack.size() < currentStack.size()) {
            return currentStack.subList(newStack.size(), currentStack.size());
        }
        return emptyList();
    }

    private List<TreeNode> pushedNodes(List<TreeNode> newStack) {
        for (int i = 0; i < currentPath.size() && i < newStack.size(); i++) {
            if (!currentPath.get(i).equals(newStack.get(i))) {
                return newStack.subList(i, newStack.size());
            }
        }
        if (newStack.size() < currentPath.size()) {
            return emptyList();
        }
        return newStack.subList(currentPath.size(), newStack.size());
    }

    private void printTestStepStarted(io.cucumber.messages.types.TestStepStarted event) {
        String timestamp = formatTimeStamp(event.getTimestamp());
        query.findTestStepBy(event).ifPresent(testStep -> {
            String name = formatTestStepName(testStep);
            String location = findPickleTestStepLocation(event, testStep)
                    .orElseGet(() -> findHookStepLocation(testStep)
                            .orElse(""));
            out.print(TEMPLATE_TEST_STARTED, timestamp, location, name);
        });
    }

    private Optional<String> findPickleTestStepLocation(TestStepStarted testStepStarted, TestStep testStep) {
        return query.findPickleStepBy(testStep)
                .flatMap(query::findStepBy)
                .flatMap(step -> query.findPickleBy(testStepStarted)
                        .map(pickle -> pickle.getUri() + ":" + step.getLocation().getLine()));
    }

    private Optional<String> findHookStepLocation(TestStep testStep) {
        return query.findHookBy(testStep)
                .map(Hook::getSourceReference)
                .map(TeamCityPlugin::formatSourceLocation);
    }

    private static String formatSourceLocation(SourceReference sourceReference) {
        return sourceReference.getJavaMethod()
                .map(TeamCityPlugin::formatJavaMethodLocation)
                .orElseGet(() -> sourceReference.getJavaStackTraceElement()
                        .map(TeamCityPlugin::formatJavaStackTraceLocation)
                        .orElse(""));
    }

    private static String formatJavaStackTraceLocation(JavaStackTraceElement javaStackTraceElement) {
        String fqClassName = javaStackTraceElement.getClassName();
        String methodName = javaStackTraceElement.getMethodName();
        return createJavaTestUri(fqClassName, sanitizeMethodName(fqClassName, methodName));
    }

    private static String formatJavaMethodLocation(JavaMethod javaMethod) {
        String fqClassName = javaMethod.getClassName();
        String methodName = javaMethod.getMethodName();
        return createJavaTestUri(fqClassName, methodName);
    }

    private static String createJavaTestUri(String fqClassName, String methodName) {
        // See:
        // https://github.com/JetBrains/intellij-community/blob/master/java/execution/impl/src/com/intellij/execution/testframework/JavaTestLocator.java
        return String.format("java:test://%s/%s", fqClassName, methodName);
    }

    private void printTestStepFinished(TestStepFinished event) {
        String timeStamp = formatTimeStamp(event.getTimestamp());
        TestStepResult testStepResult = event.getTestStepResult();
        long duration = toDuration(testStepResult.getDuration()).toMillis();

        query.findTestStepBy(event).ifPresent(testStep -> {
            String name = formatTestStepName(testStep);

            Optional<Exception> error = testStepResult.getException();
            TestStepResultStatus status = testStepResult.getStatus();
            switch (status) {
                case SKIPPED: {
                    String message = error.flatMap(Exception::getMessage).orElse("Step skipped");
                    out.print(TEMPLATE_TEST_IGNORED, timeStamp, duration, message, name);
                    break;
                }
                case PENDING: {
                    String details = error.flatMap(Exception::getMessage).orElse("");
                    out.print(TEMPLATE_TEST_FAILED, timeStamp, duration, "Step pending", details, name);
                    break;
                }
                case UNDEFINED: {
                    String snippets = findSnippets(currentPickle).orElse("");
                    out.print(TEMPLATE_TEST_FAILED, timeStamp, duration, "Step undefined", snippets, name);
                    break;
                }
                case AMBIGUOUS:
                case FAILED: {
                    String details = error.flatMap(Exception::getStackTrace).orElse("");
                    String message = error.flatMap(Exception::getMessage).orElse(null);
                    if (message == null) {
                        out.print(TEMPLATE_TEST_FAILED, timeStamp, duration, "Step failed", details, name);
                        break;
                    }
                    ComparisonFailure comparisonFailure = ComparisonFailure.parse(message.trim());
                    if (comparisonFailure == null) {
                        out.print(TEMPLATE_TEST_FAILED, timeStamp, duration, "Step failed", details, name);
                        break;
                    }
                    out.print(TEMPLATE_TEST_COMPARISON_FAILED, timeStamp, duration, "Step failed", details,
                        comparisonFailure.getExpected(), comparisonFailure.getActual(), name);
                    break;
                }
                default:
                    break;
            }
            out.print(TEMPLATE_TEST_FINISHED, timeStamp, duration, name);
        });
    }

    private String formatTestStepName(TestStep testStep) {
        return query.findPickleStepBy(testStep)
                .map(PickleStep::getText)
                .orElseGet(() -> query.findHookBy(testStep)
                        .map(TeamCityPlugin::formatHookStepName)
                        .orElse("Unknown step"));
    }

    private static String formatHookStepName(Hook hook) {
        // TODO: Use hook name.
        SourceReference sourceReference = hook.getSourceReference();
        return sourceReference.getJavaMethod()
                .map(javaMethod -> formatJavaMethodName(hook, javaMethod))
                .orElseGet(() -> sourceReference.getJavaStackTraceElement()
                        .map(javaStackTraceElement -> formatJavaStackTraceName(hook, javaStackTraceElement))
                        .orElse("Unknown"));
    }

    private static String formatJavaStackTraceName(Hook hook, JavaStackTraceElement javaStackTraceElement) {
        String methodName = javaStackTraceElement.getMethodName();
        String fqClassName = javaStackTraceElement.getClassName();
        String hookName = getHookName(hook);
        String sanitizeMethodName = sanitizeMethodName(fqClassName, methodName);
        return String.format("%s(%s)", hookName, sanitizeMethodName);
    }

    private static String sanitizeMethodName(String fqClassName, String methodName) {
        if (!methodName.equals("<init>")) {
            return methodName;
        }
        // Replace constructor name, not recognized by IDEA.
        int classNameIndex = fqClassName.lastIndexOf('.');
        if (classNameIndex > 0) {
            return fqClassName.substring(classNameIndex + 1);
        }
        return methodName;
    }

    private static String formatJavaMethodName(Hook hook, JavaMethod javaMethod) {
        String methodName = javaMethod.getMethodName();
        String hookName = getHookName(hook);
        return String.format("%s(%s)", hookName, methodName);
    }

    private static String getHookName(Hook hook) {
        return hook.getType().map(
            hookType -> {
                switch (hookType) {
                    case BEFORE_TEST_RUN:
                        return "BeforeAll";
                    case AFTER_TEST_RUN:
                        return "AfterAll";
                    case BEFORE_TEST_CASE:
                        return "Before";
                    case AFTER_TEST_CASE:
                        return "After";
                    case BEFORE_TEST_STEP:
                        return "BeforeStep";
                    case AFTER_TEST_STEP:
                        return "AfterStep";
                    default:
                        return "Unknown";
                }
            }).orElse("Unknown");
    }

    private Optional<String> findSnippets(Pickle pickle) {
        return query.findLocationOf(pickle)
                .map(location -> {
                    URI uri = URI.create(pickle.getUri());
                    List<Suggestion> suggestionForTestCase = suggestions.stream()
                            .filter(suggestion -> isSuggestionForPickleAt(suggestion, uri, location))
                            .map(SnippetsSuggestedEvent::getSuggestion)
                            .collect(toList());
                    return createMessage(suggestionForTestCase);
                });
    }

    private static boolean isSuggestionForPickleAt(SnippetsSuggestedEvent suggestion, URI uri, Location location) {
        return suggestion.getUri().equals(uri) && suggestion.getTestCaseLocation().getLine() == location.getLine();
    }

    private static String createMessage(Collection<Suggestion> suggestions) {
        if (suggestions.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("You can implement this step");
        if (suggestions.size() > 1) {
            sb.append(" and ").append(suggestions.size() - 1).append(" other step(s)");
        }
        sb.append(" using the snippet(s) below:\n\n");
        String snippets = suggestions
                .stream()
                .map(Suggestion::getSnippets)
                .flatMap(Collection::stream)
                .distinct()
                .collect(joining("\n", "", "\n"));
        sb.append(snippets);
        return sb.toString();
    }

    private void printTestCaseFinished(TestCaseFinished event) {
        String timestamp = formatTimeStamp(event.getTimestamp());
        out.print(TEMPLATE_PROGRESS_TEST_FINISHED, timestamp);
        finishNode(timestamp, currentPath.remove(currentPath.size() - 1));
        this.currentPickle = null;
    }

    private void printTestRunFinished(io.cucumber.messages.types.TestRunFinished event) {
        String timestamp = formatTimeStamp(event.getTimestamp());
        out.print(TEMPLATE_PROGRESS_COUNTING_FINISHED, timestamp);

        List<TreeNode> emptyPath = new ArrayList<>();
        poppedNodes(emptyPath).forEach(node -> finishNode(timestamp, node));
        currentPath = emptyPath;

        printBeforeAfterAllResult(event, timestamp);
        out.print(TEMPLATE_TEST_RUN_FINISHED, timestamp);
    }

    private void printBeforeAfterAllResult(io.cucumber.messages.types.TestRunFinished event, String timestamp) {
        Optional<Exception> error = event.getException();
        if (!error.isPresent()) {
            return;
        }
        // Use dummy test to display before all after all failures
        String name = "Before All/After All";
        out.print(TEMPLATE_BEFORE_ALL_AFTER_ALL_STARTED, timestamp, name);
        String details = error.flatMap(Exception::getStackTrace).orElse("");
        out.print(TEMPLATE_BEFORE_ALL_AFTER_ALL_FAILED, timestamp, "Before All/After All failed", details, name);
        out.print(TEMPLATE_BEFORE_ALL_AFTER_ALL_FINISHED, timestamp, name);
    }

    private void handleSnippetSuggested(SnippetsSuggestedEvent event) {
        suggestions.add(event);
    }

    private void handleEmbedEvent(Attachment event) {
        switch (event.getContentEncoding()) {
            case IDENTITY:
                out.print(TEMPLATE_ATTACH_WRITE_EVENT, "Write event:\n" + event.getBody() + "\n");
                return;
            case BASE64:
                String name = event.getFileName().map(s -> s + " ").orElse("");
                out.print(TEMPLATE_ATTACH_WRITE_EVENT,
                    "Embed event: " + name + "[" + event.getMediaType() + " " + (event.getBody().length() / 4) * 3
                            + " bytes]\n");
                return;
            default:
                // Ignore.
        }
    }

    private static String formatTimeStamp(Timestamp timestamp) {
        ZonedDateTime date = Convertor.toInstant(timestamp).atZone(ZoneOffset.UTC);
        return DATE_FORMAT.format(date);
    }

    private static class TeamCityCommandWriter implements Closeable {
        private final PrintStream out;

        public TeamCityCommandWriter(PrintStream out) {
            this.out = out;
        }

        private void print(String command, Object... args) {
            out.println(formatCommand(command, args));
        }

        private String formatCommand(String command, Object... parameters) {
            String[] escapedParameters = new String[parameters.length];
            for (int i = 0; i < escapedParameters.length; i++) {
                escapedParameters[i] = escape(parameters[i].toString());
            }

            return String.format(command, (Object[]) escapedParameters);
        }

        private String escape(String source) {
            if (source == null) {
                return "";
            }
            return source
                    .replace("|", "||")
                    .replace("'", "|'")
                    .replace("\n", "|n")
                    .replace("\r", "|r")
                    .replace("[", "|[")
                    .replace("]", "|]");
        }

        @Override
        public void close() {
            out.close();
        }
    }

    private static class ComparisonFailure {

        private static final Pattern[] COMPARE_PATTERNS = new Pattern[] {
                // Hamcrest 2 MatcherAssert.assertThat
                Pattern.compile("expected: (.*)(?:\r\n|\r|\n) {5}but: was (.*)$",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
                // AssertJ 3 ShouldBeEqual.smartErrorMessage
                Pattern.compile("expected: (.*)(?:\r\n|\r|\n) but was: (.*)$",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
                // JUnit 5 AssertionFailureBuilder
                Pattern.compile("expected: <(.*)> but was: <(.*)>$",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
                // JUnit 4 Assert.assertEquals
                Pattern.compile("expected:\\s?<(.*)> but was:\\s?<(.*)>$",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
                // TestNG 7 Assert.assertEquals
                Pattern.compile("expected \\[(.*)] but found \\[(.*)]\n$",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
        };

        static ComparisonFailure parse(String message) {
            for (Pattern pattern : COMPARE_PATTERNS) {
                ComparisonFailure result = parse(message, pattern);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }

        static ComparisonFailure parse(String message, Pattern pattern) {
            final Matcher matcher = pattern.matcher(message);
            if (!matcher.find()) {
                return null;
            }
            String expected = matcher.group(1);
            String actual = matcher.group(2);
            return new ComparisonFailure(expected, actual);
        }

        private final String expected;

        private final String actual;

        ComparisonFailure(String expected, String actual) {
            this.expected = requireNonNull(expected);
            this.actual = requireNonNull(actual);
        }

        public String getExpected() {
            return expected;
        }

        public String getActual() {
            return actual;
        }
    }

    private static final class TreeNode {
        private final String name;
        private final String uri;
        private final io.cucumber.messages.types.Location location;

        private TreeNode(String name, String uri, io.cucumber.messages.types.Location location) {
            this.name = name;
            this.uri = uri;
            this.location = location;
        }

        public String getName() {
            return name;
        }

        public String getUri() {
            return uri;
        }

        public io.cucumber.messages.types.Location getLocation() {
            return location;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass())
                return false;
            TreeNode that = (TreeNode) o;
            return Objects.equals(name, that.name) && Objects.equals(uri, that.uri)
                    && Objects.equals(location, that.location);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, uri, location);
        }
    }

    private static class PathCollector implements LineageReducer.Collector<List<TreeNode>> {
        // There are at most 5 levels to a feature file.
        private final List<TreeNode> path = new ArrayList<>(5);
        private String uri;
        private String scenarioName;
        private int examplesIndex;
        private boolean isExample;

        @Override
        public void add(GherkinDocument document) {
            uri = document.getUri().orElse("");
        }

        @Override
        public void add(Feature feature) {
            String name = getNameOrKeyword(feature.getName(), feature.getKeyword());
            path.add(new TreeNode(name, uri, feature.getLocation()));
        }

        @Override
        public void add(Rule rule) {
            String name = getNameOrKeyword(rule.getName(), rule.getKeyword());
            path.add(new TreeNode(name, uri, rule.getLocation()));
        }

        @Override
        public void add(Scenario scenario) {
            String name = getNameOrKeyword(scenario.getName(), scenario.getKeyword());
            path.add(new TreeNode(name, uri, scenario.getLocation()));
            scenarioName = name;
        }

        @Override
        public void add(Examples examples, int index) {
            String name = getNameOrKeyword(examples.getName(), examples.getKeyword());
            path.add(new TreeNode(name, uri, examples.getLocation()));
            examplesIndex = index;
        }

        @Override
        public void add(TableRow example, int index) {
            isExample = true;
            String name = "#" + (examplesIndex + 1) + "." + (index + 1);
            path.add(new TreeNode(name, uri, example.getLocation()));
        }

        @Override
        public void add(Pickle pickle) {
            // Case 1: Pickles from a scenario outline
            if (isExample) {
                String pickleName = pickle.getName();
                boolean parameterized = !scenarioName.equals(pickleName);
                if (parameterized) {
                    TreeNode example = path.remove(path.size() - 1);
                    String parameterizedExampleName = example.getName() + ": " + pickleName;
                    path.add(new TreeNode(parameterizedExampleName, example.getUri(), example.getLocation()));
                }
            }
            // Case 2: Pickles from a scenario
            // Nothing to do, scenario name and pickle name are the same.
        }

        @Override
        public List<TreeNode> finish() {
            return path;
        }

        private static String getNameOrKeyword(String name, String keyword) {
            if (!name.isEmpty()) {
                return name;
            }
            if (!keyword.isEmpty()) {
                return keyword;
            }
            // Always return a non-empty string otherwise the tree diagram is
            // hard to click.
            return "Unknown";
        }
    }
}
