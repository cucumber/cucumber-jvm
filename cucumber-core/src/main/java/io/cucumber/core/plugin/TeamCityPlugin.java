package io.cucumber.core.plugin;

import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EmbedEvent;
import io.cucumber.plugin.event.Event;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.HookType;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.SnippetsSuggestedEvent.Suggestion;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestSourceParsed;
import io.cucumber.plugin.event.TestStep;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import io.cucumber.plugin.event.WriteEvent;

import java.io.PrintStream;
import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.cucumber.core.exception.ExceptionUtils.printStackTrace;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

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

    private static final Pattern ANNOTATION_GLUE_CODE_LOCATION_PATTERN = Pattern.compile("^(.*)\\.(.*)\\([^:]*\\)");
    private static final Pattern LAMBDA_GLUE_CODE_LOCATION_PATTERN = Pattern.compile("^(.*)\\.(.*)\\(.*:.*\\)");

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

    private final PrintStream out;
    private final List<SnippetsSuggestedEvent> suggestions = new ArrayList<>();
    private final Map<URI, Collection<Node>> parsedTestSources = new HashMap<>();
    private List<Node> currentStack = new ArrayList<>();
    private TestCase currentTestCase;

    @SuppressWarnings("unused") // Used by PluginFactory
    public TeamCityPlugin() {
        // This plugin prints markers for Team City and IntelliJ IDEA that
        // allows them to associate the output to specific test cases. Printing
        // to system out - and potentially mixing with other formatters - is
        // intentional.
        this(System.out);
    }

    TeamCityPlugin(PrintStream out) {
        this.out = out;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestRunStarted.class, this::printTestRunStarted);
        publisher.registerHandlerFor(TestCaseStarted.class, this::printTestCaseStarted);
        publisher.registerHandlerFor(TestStepStarted.class, this::printTestStepStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::printTestStepFinished);
        publisher.registerHandlerFor(TestCaseFinished.class, this::printTestCaseFinished);
        publisher.registerHandlerFor(TestRunFinished.class, this::printTestRunFinished);
        publisher.registerHandlerFor(SnippetsSuggestedEvent.class, this::handleSnippetSuggested);
        publisher.registerHandlerFor(EmbedEvent.class, this::handleEmbedEvent);
        publisher.registerHandlerFor(WriteEvent.class, this::handleWriteEvent);
        publisher.registerHandlerFor(TestSourceParsed.class, this::handleTestSourceParsed);
    }

    private void handleTestSourceParsed(TestSourceParsed event) {
        parsedTestSources.put(event.getUri(), event.getNodes());
    }

    private void printTestRunStarted(TestRunStarted event) {
        String timestamp = extractTimeStamp(event);
        print(TEMPLATE_ENTER_THE_MATRIX, timestamp);
        print(TEMPLATE_TEST_RUN_STARTED, timestamp);
        print(TEMPLATE_PROGRESS_COUNTING_STARTED, timestamp);
    }

    private String extractTimeStamp(Event event) {
        ZonedDateTime date = event.getInstant().atZone(ZoneOffset.UTC);
        return DATE_FORMAT.format(date);
    }

    private void printTestCaseStarted(TestCaseStarted event) {
        TestCase testCase = event.getTestCase();
        URI uri = testCase.getUri();
        String timestamp = extractTimeStamp(event);

        Location location = testCase.getLocation();
        Predicate<Node> withLocation = candidate -> location.equals(candidate.getLocation());
        List<Node> path = parsedTestSources.get(uri)
                .stream()
                .map(node -> node.findPathTo(withLocation))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(emptyList());

        poppedNodes(path).forEach(node -> finishNode(timestamp, node));
        pushedNodes(path).forEach(node -> startNode(uri, timestamp, node));
        this.currentStack = path;
        this.currentTestCase = testCase;

        print(TEMPLATE_PROGRESS_TEST_STARTED, timestamp);
    }

    private void startNode(URI uri, String timestamp, Node node) {
        Supplier<String> keyword = () -> node.getKeyword().orElse("Unknown");
        String name = node.getName().orElseGet(keyword);
        String location = uri + ":" + node.getLocation().getLine();
        print(TEMPLATE_TEST_SUITE_STARTED, timestamp, location, name);
    }

    private void finishNode(String timestamp, Node node) {
        Supplier<String> keyword = () -> node.getKeyword().orElse("Unknown");
        String name = node.getName().orElseGet(keyword);
        print(TEMPLATE_TEST_SUITE_FINISHED, timestamp, name);
    }

    private List<Node> poppedNodes(List<Node> newStack) {
        List<Node> nodes = new ArrayList<>(reversedPoppedNodes(currentStack, newStack));
        Collections.reverse(nodes);
        return nodes;
    }

    private List<Node> reversedPoppedNodes(List<Node> currentStack, List<Node> newStack) {
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

    private List<Node> pushedNodes(List<Node> newStack) {
        for (int i = 0; i < currentStack.size() && i < newStack.size(); i++) {
            if (!currentStack.get(i).equals(newStack.get(i))) {
                return newStack.subList(i, newStack.size());
            }
        }
        if (newStack.size() < currentStack.size()) {
            return emptyList();
        }
        return newStack.subList(currentStack.size(), newStack.size());
    }

    private void printTestStepStarted(TestStepStarted event) {
        String timestamp = extractTimeStamp(event);
        String name = extractName(event.getTestStep());
        String location = extractLocation(event);
        print(TEMPLATE_TEST_STARTED, timestamp, location, name);
    }

    private String extractLocation(TestStepStarted event) {
        TestStep testStep = event.getTestStep();
        if (testStep instanceof PickleStepTestStep) {
            PickleStepTestStep pickleStepTestStep = (PickleStepTestStep) testStep;
            return pickleStepTestStep.getUri() + ":" + pickleStepTestStep.getStep().getLine();
        }
        if (testStep instanceof HookTestStep) {
            return formatHookStepLocation(
                (HookTestStep) testStep,
                javaTestLocationUri(),
                TestStep::getCodeLocation);
        }
        return testStep.getCodeLocation();
    }

    private static BiFunction<String, String, String> javaTestLocationUri() {
        return (fqDeclaringClassName, classOrMethodName) -> String.format("java:test://%s/%s", fqDeclaringClassName,
            classOrMethodName);
    }

    private String formatHookStepLocation(
            HookTestStep hookTestStep, BiFunction<String, String, String> hookStepCase,
            Function<HookTestStep, String> defaultHookName
    ) {
        Matcher javaMatcher = ANNOTATION_GLUE_CODE_LOCATION_PATTERN.matcher(hookTestStep.getCodeLocation());
        if (javaMatcher.matches()) {
            String fqDeclaringClassName = javaMatcher.group(1);
            String methodName = javaMatcher.group(2);
            return hookStepCase.apply(fqDeclaringClassName, methodName);
        }
        Matcher java8Matcher = LAMBDA_GLUE_CODE_LOCATION_PATTERN.matcher(hookTestStep.getCodeLocation());
        if (java8Matcher.matches()) {
            String fqDeclaringClassName = java8Matcher.group(1);
            String declaringClassName;
            int indexOfPackageSeparator = fqDeclaringClassName.lastIndexOf(".");
            if (indexOfPackageSeparator != -1) {
                declaringClassName = fqDeclaringClassName.substring(indexOfPackageSeparator + 1);
            } else {
                declaringClassName = fqDeclaringClassName;
            }
            return hookStepCase.apply(fqDeclaringClassName, declaringClassName);
        }
        return defaultHookName.apply(hookTestStep);
    }

    private void printTestStepFinished(TestStepFinished event) {
        String timeStamp = extractTimeStamp(event);
        long duration = extractDuration(event.getResult());
        String name = extractName(event.getTestStep());

        Throwable error = event.getResult().getError();
        Status status = event.getResult().getStatus();
        switch (status) {
            case SKIPPED: {
                String message = error == null ? "Step skipped" : error.getMessage();
                print(TEMPLATE_TEST_IGNORED, timeStamp, duration, message, name);
                break;
            }
            case PENDING: {
                String details = error == null ? "" : error.getMessage();
                print(TEMPLATE_TEST_FAILED, timeStamp, duration, "Step pending", details, name);
                break;
            }
            case UNDEFINED: {
                String snippets = getSnippets(currentTestCase);
                print(TEMPLATE_TEST_FAILED, timeStamp, duration, "Step undefined", snippets, name);
                break;
            }
            case AMBIGUOUS:
            case FAILED: {
                String details = printStackTrace(error);
                String message = error.getMessage();
                if (message == null) {
                    print(TEMPLATE_TEST_FAILED, timeStamp, duration, "Step failed", details, name);
                    break;
                }
                ComparisonFailure comparisonFailure = ComparisonFailure.parse(message.trim());
                if (comparisonFailure == null) {
                    print(TEMPLATE_TEST_FAILED, timeStamp, duration, "Step failed", details, name);
                    break;
                }
                print(TEMPLATE_TEST_COMPARISON_FAILED, timeStamp, duration, "Step failed", details,
                    comparisonFailure.getExpected(), comparisonFailure.getActual(), name);
                break;
            }
            default:
                break;
        }
        print(TEMPLATE_TEST_FINISHED, timeStamp, duration, name);
    }

    private String getHookName(HookTestStep hook) {
        HookType hookType = hook.getHookType();
        switch (hookType) {
            case BEFORE:
                return "Before";
            case AFTER:
                return "After";
            case BEFORE_STEP:
                return "BeforeStep";
            case AFTER_STEP:
                return "AfterStep";
            default:
                return hookType.name().toLowerCase(Locale.US);
        }
    }

    private String extractName(TestStep testStep) {
        if (testStep instanceof PickleStepTestStep) {
            PickleStepTestStep pickleStepTestStep = (PickleStepTestStep) testStep;
            return pickleStepTestStep.getStep().getText();
        }
        if (testStep instanceof HookTestStep) {
            HookTestStep hookTestStep = (HookTestStep) testStep;
            return formatHookStepLocation(
                hookTestStep,
                hookNameFormat(hookTestStep),
                this::getHookName);
        }
        return "Unknown step";
    }

    private BiFunction<String, String, String> hookNameFormat(HookTestStep hookTestStep) {
        return (fqDeclaringClassName, classOrMethodName) -> String.format("%s(%s)", getHookName(hookTestStep),
            classOrMethodName);
    }

    private String getSnippets(TestCase testCase) {
        URI uri = testCase.getUri();
        Location location = testCase.getLocation();
        List<Suggestion> suggestionForTestCase = suggestions.stream()
                .filter(suggestion -> suggestion.getUri().equals(uri) &&
                        suggestion.getTestCaseLocation().equals(location))
                .map(SnippetsSuggestedEvent::getSuggestion)
                .collect(Collectors.toList());
        return createMessage(suggestionForTestCase);
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
        String timestamp = extractTimeStamp(event);
        print(TEMPLATE_PROGRESS_TEST_FINISHED, timestamp);
        finishNode(timestamp, currentStack.remove(currentStack.size() - 1));
        this.currentTestCase = null;
    }

    private long extractDuration(Result result) {
        return result.getDuration().toMillis();
    }

    private void printTestRunFinished(TestRunFinished event) {
        String timestamp = extractTimeStamp(event);
        print(TEMPLATE_PROGRESS_COUNTING_FINISHED, timestamp);

        List<Node> emptyStack = new ArrayList<>();
        poppedNodes(emptyStack).forEach(node -> finishNode(timestamp, node));
        currentStack = emptyStack;

        printBeforeAfterAllResult(event, timestamp);
        print(TEMPLATE_TEST_RUN_FINISHED, timestamp);
    }

    private void printBeforeAfterAllResult(TestRunFinished event, String timestamp) {
        Throwable error = event.getResult().getError();
        if (error == null) {
            return;
        }
        // Use dummy test to display before all after all failures
        String name = "Before All/After All";
        print(TEMPLATE_BEFORE_ALL_AFTER_ALL_STARTED, timestamp, name);
        String details = printStackTrace(error);
        print(TEMPLATE_BEFORE_ALL_AFTER_ALL_FAILED, timestamp, "Before All/After All failed", details, name);
        print(TEMPLATE_BEFORE_ALL_AFTER_ALL_FINISHED, timestamp, name);
    }

    private void handleSnippetSuggested(SnippetsSuggestedEvent event) {
        suggestions.add(event);
    }

    private void handleEmbedEvent(EmbedEvent event) {
        String name = event.getName() == null ? "" : event.getName() + " ";
        print(TEMPLATE_ATTACH_WRITE_EVENT,
            "Embed event: " + name + "[" + event.getMediaType() + " " + event.getData().length + " bytes]\n");
    }

    private void handleWriteEvent(WriteEvent event) {
        print(TEMPLATE_ATTACH_WRITE_EVENT, "Write event:\n" + event.getText() + "\n");
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

    private static class ComparisonFailure {

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
}
