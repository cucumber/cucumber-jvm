package cucumber.runtime.junit;

class SkippedThrowable extends Throwable {
    private static final long serialVersionUID = 1L;

    public SkippedThrowable(NotificationLevel scenarioOrStep) {
        super(String.format("This %s is skipped", scenarioOrStep.lowerCaseName()), null, false, false);
    }
}
