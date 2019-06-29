package io.cucumber.core.runner;

import io.cucumber.core.event.Result;
import io.cucumber.core.event.Status;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;

import static io.cucumber.core.event.Status.AMBIGUOUS;
import static io.cucumber.core.event.Status.FAILED;
import static io.cucumber.core.event.Status.PASSED;
import static io.cucumber.core.event.Status.PENDING;
import static io.cucumber.core.event.Status.SKIPPED;
import static io.cucumber.core.event.Status.UNDEFINED;
import static java.time.Duration.ZERO;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ResultTest {

    @Test
    public void severity_from_low_to_high_is_passed_skipped_pending_undefined_ambiguous_failed() {
        Result passed = new Result(PASSED, ZERO, null);
        Result skipped = new Result(SKIPPED, ZERO, null);
        Result pending = new Result(PENDING, ZERO, null);
        Result ambiguous = new Result(AMBIGUOUS, ZERO, null);
        Result undefined = new Result(UNDEFINED, ZERO, null);
        Result failed = new Result(FAILED, ZERO, null);

        List<Result> results = asList(pending, passed, skipped, failed, ambiguous, undefined);

        sort(results, Comparator.comparing(Result::getStatus));

        assertThat(results, equalTo(asList(passed, skipped, pending, undefined, ambiguous, failed)));
    }

    @Test
    public void passed_result_is_always_ok() {
        Result passedResult = new Result(PASSED, ZERO, null);

        assertTrue(passedResult.getStatus().isOk(isStrict(false)));
        assertTrue(passedResult.getStatus().isOk(isStrict(true)));
    }

    @Test
    public void skipped_result_is_always_ok() {
        Result skippedResult = new Result(SKIPPED, ZERO, null);

        assertTrue(skippedResult.getStatus().isOk(isStrict(false)));
        assertTrue(skippedResult.getStatus().isOk(isStrict(true)));
    }

    @Test
    public void failed_result_is_never_ok() {
        Result failedResult = new Result(FAILED, ZERO, null);

        assertFalse(failedResult.getStatus().isOk(isStrict(false)));
        assertFalse(failedResult.getStatus().isOk(isStrict(true)));
    }

    @Test
    public void undefined_result_is_only_ok_when_not_strict() {
        Result undefinedResult = new Result(UNDEFINED, ZERO, null);

        assertTrue(undefinedResult.getStatus().isOk(isStrict(false)));
        assertFalse(undefinedResult.getStatus().isOk(isStrict(true)));
    }

    @Test
    public void pending_result_is_only_ok_when_not_strict() {
        Result pendingResult = new Result(PENDING, ZERO, null);

        assertTrue(pendingResult.getStatus().isOk(isStrict(false)));
        assertFalse(pendingResult.getStatus().isOk(isStrict(true)));
    }

    @Test
    public void is_query_returns_true_for_the_status_of_the_result_object() {
        int checkCount = 0;
        for (Status status : Status.values()) {
            Result result = new Result(status, ZERO, null);

            assertTrue(result.getStatus().is(result.getStatus()));
            checkCount += 1;
        }
        assertTrue("No checks performed", checkCount > 0);
    }

    @Test
    public void is_query_returns_false_for_statuses_different_from_the_status_of_the_result_object() {
        int checkCount = 0;
        for (Status resultStatus : Status.values()) {
            Result result = new Result(resultStatus, ZERO, null);
            for (Status status : Status.values()) {
                if (status != resultStatus) {
                    assertFalse(result.getStatus().is(status));
                    checkCount += 1;
                }
            }
        }
        assertTrue("No checks performed", checkCount > 0);
    }

    private boolean isStrict(boolean value) {
        return value;
    }
}
