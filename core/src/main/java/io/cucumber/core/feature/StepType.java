package io.cucumber.core.feature;

public enum StepType {
    GIVEN, WHEN, THEN, AND, BUT, OTHER;

    private static final String ASTRIX_KEY_WORD = "* ";

    public boolean isGivenWhenThen() {
        return this == GIVEN || this == WHEN || this == THEN;
    }

    static boolean isAstrix(String stepType) {
        return ASTRIX_KEY_WORD.equals(stepType);
    }
}
