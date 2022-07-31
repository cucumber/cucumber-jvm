package io.cucumber.plugin.event;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public enum Status {
    PASSED,
    SKIPPED,
    PENDING,
    UNDEFINED,
    AMBIGUOUS,
    FAILED,
    UNUSED;

    /**
     * Does this state allow the build to pass
     *
     * @param      isStrict should this result be evaluated strictly? Ignored.
     * @return              true if this result does not fail the build
     * @deprecated          please use {@link #isOk()}}
     */
    @Deprecated
    public boolean isOk(boolean isStrict) {
        return isOk();
    }

    /**
     * Does this state allow the build to pass
     *
     * @return true if this result does not fail the build
     */
    public boolean isOk() {
        return is(Status.PASSED) || is(Status.SKIPPED);
    }

    public boolean is(Status status) {
        return this == status;
    }

}
