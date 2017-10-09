package cucumber.api.event;

public interface EventPublisher {

    /**
     * Registers an event handler for a specific event.
     *
     * The available events types are:
     * <ul>
     * <li>{@link TestRunStarted} - the first event sent.
     * <li>{@link TestSourceRead} - sent for each feature file read, contains the feature file source.
     * <li>{@link SnippetsSuggestedEvent} - sent for each step that could not be matched to a step definition, contains the raw snippets for the step.
     * <li> {@link TestCaseStarted} - sent before starting the execution of a Test Case(/Pickle/Scenario), contains the Test Case
     * <li>{@link TestStepStarted} - sent before starting the execution of a Test Step, contains the Test Step
     * <li>{@link EmbedEvent} - calling scenario.embed in a hook triggers this event.
     * <li>{@link WriteEvent} - calling scenario.write in a hook triggers this event.
     * <li>{@link TestStepFinished} - sent after the execution of a Test Step, contains the Test Step and its Result.
     * <li>{@link TestCaseFinished} - sent after the execution of a Test Case(/Pickle/Scenario), contains the Test Case and its Result.
     * <li>{@link TestRunFinished} - the last event sent.
     * </ul>
     *
     *
     * @param eventType the event type for which the handler is being registered
     * @param handler the event handler
     * @param <T> the event type
     */
    <T extends Event> void registerHandlerFor(Class<T> eventType, EventHandler<T> handler);
}
