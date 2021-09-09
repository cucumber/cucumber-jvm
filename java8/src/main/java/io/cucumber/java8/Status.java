package io.cucumber.java8;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public enum Status {
    PASSED,
    SKIPPED,
    PENDING,
    UNDEFINED,
    AMBIGUOUS,
    FAILED,
    UNUSED
}
