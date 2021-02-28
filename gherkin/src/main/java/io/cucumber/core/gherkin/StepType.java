package io.cucumber.core.gherkin;

public enum StepType {
    GIVEN, WHEN, THEN, AND, BUT, OTHER;

    private static final String ASTRIX_KEY_WORD = "* ";

    public static boolean isAstrix(String stepType) {
        return ASTRIX_KEY_WORD.equals(stepType);
    }

    public boolean isGivenWhenThen() {
        return this == GIVEN || this == WHEN || this == THEN;
    }
}
