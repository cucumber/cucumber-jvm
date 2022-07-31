package io.cucumber.core.runner;

import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static io.cucumber.plugin.event.Status.AMBIGUOUS;
import static io.cucumber.plugin.event.Status.FAILED;
import static io.cucumber.plugin.event.Status.PASSED;
import static io.cucumber.plugin.event.Status.PENDING;
import static io.cucumber.plugin.event.Status.SKIPPED;
import static io.cucumber.plugin.event.Status.UNDEFINED;
import static java.time.Duration.ZERO;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultTest {

    @Test
    void severity_from_low_to_high_is_passed_skipped_pending_undefined_ambiguous_failed() {
        Result passed = new Result(PASSED, ZERO, null);
        Result skipped = new Result(SKIPPED, ZERO, null);
        Result pending = new Result(PENDING, ZERO, null);
        Result ambiguous = new Result(AMBIGUOUS, ZERO, null);
        Result undefined = new Result(UNDEFINED, ZERO, null);
        Result failed = new Result(FAILED, ZERO, null);

        List<Result> results = asList(pending, passed, skipped, failed, ambiguous, undefined);

        results.sort(Comparator.comparing(Result::getStatus));

        assertThat(results, equalTo(asList(passed, skipped, pending, undefined, ambiguous, failed)));
    }

    @Test
    void passed_result_is_ok() {
        Result passedResult = new Result(PASSED, ZERO, null);
        assertTrue(passedResult.getStatus().isOk());
    }

    @Test
    void skipped_result_is_ok() {
        Result skippedResult = new Result(SKIPPED, ZERO, null);
        assertTrue(skippedResult.getStatus().isOk());
    }

    @Test
    void failed_result_is_not_ok() {
        Result failedResult = new Result(FAILED, ZERO, null);
        assertFalse(failedResult.getStatus().isOk());
    }

    @Test
    void is_query_returns_true_for_the_status_of_the_result_object() {
        int checkCount = 0;
        for (Status status : Status.values()) {
            Result result = new Result(status, ZERO, null);

            assertTrue(result.getStatus().is(result.getStatus()));
            checkCount += 1;
        }
        assertThat("No checks performed", checkCount > 0, is(equalTo(true)));
    }

    @Test
    void is_query_returns_false_for_statuses_different_from_the_status_of_the_result_object() {
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
        assertThat("No checks performed", checkCount > 0, is(equalTo(true)));
    }

}
