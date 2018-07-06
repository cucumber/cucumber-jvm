package cucumber.runtime.formatter;

/**
 * Indicates that there was a missing step in the execution of the scenario lifecycle.
 */
final class MissingStepDefinitionError extends AssertionError {

    /**
     * Creates a new instance for the given snippet.
     *
     * @param snippet the suggested snippet which could be implemented to avoid this exception
     */
    MissingStepDefinitionError(final String snippet) {
        super(String.format("\n\n%s", snippet));
    }
}
