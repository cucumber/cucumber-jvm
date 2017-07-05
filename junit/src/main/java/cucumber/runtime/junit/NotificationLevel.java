package cucumber.runtime.junit;

public enum NotificationLevel {
    SCENARIO,
    STEP;

    String lowerCaseName() {
        return name().toLowerCase();
    }
}
