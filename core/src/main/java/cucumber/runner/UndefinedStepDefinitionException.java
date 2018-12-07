package cucumber.runner;

final class UndefinedStepDefinitionException extends RuntimeException {

    UndefinedStepDefinitionException(String stepText) {
        super("Step not defined: '" + stepText + "'");
    }
}
