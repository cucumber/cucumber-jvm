package io.cucumber.core.event;

public enum Status {
    PASSED,
    SKIPPED,
    PENDING,
    UNDEFINED,
    AMBIGUOUS,
    FAILED,
    UNUSED;

    public boolean is(Status status) {
        return this == status;
    }

    public boolean isOk(boolean isStrict) {
        return hasAlwaysOkStatus() || !isStrict && hasOkWhenNotStrictStatus();
    }

    private boolean hasAlwaysOkStatus() {
        return is(Status.PASSED) || is(Status.SKIPPED);
    }

    private boolean hasOkWhenNotStrictStatus() {
        return is(Status.UNDEFINED) || is(Status.PENDING);
    }

}
