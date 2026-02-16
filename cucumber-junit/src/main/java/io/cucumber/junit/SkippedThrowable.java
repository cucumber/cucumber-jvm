package io.cucumber.junit;

import java.io.Serial;

import static java.util.Locale.ROOT;

final class SkippedThrowable extends Throwable {

    @Serial
    private static final long serialVersionUID = 1L;

    SkippedThrowable(NotificationLevel scenarioOrStep) {
        super("This %s is skipped".formatted(scenarioOrStep.lowerCaseName()), null, false, false);
    }

    enum NotificationLevel {
        SCENARIO,
        STEP;

        String lowerCaseName() {
            return name().toLowerCase(ROOT);
        }
    }

}
