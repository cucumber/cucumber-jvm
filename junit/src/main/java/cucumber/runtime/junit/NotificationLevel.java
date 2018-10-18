package cucumber.runtime.junit;

import static java.util.Locale.ROOT;

public enum NotificationLevel {
    SCENARIO,
    STEP;

    String lowerCaseName() {
        return name().toLowerCase(ROOT);
    }
}
