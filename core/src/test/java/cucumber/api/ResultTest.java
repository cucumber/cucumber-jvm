package cucumber.api;

import org.junit.Test;

import java.util.List;

import static cucumber.api.Result.SEVERITY;
import static cucumber.api.Result.Type.AMBIGUOUS;
import static cucumber.api.Result.Type.FAILED;
import static cucumber.api.Result.Type.PASSED;
import static cucumber.api.Result.Type.PENDING;
import static cucumber.api.Result.Type.SKIPPED;
import static cucumber.api.Result.Type.UNDEFINED;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ResultTest {

    @Test
    public void severity_from_low_to_high_is_passed_skipped_pending_undefined_ambiguous_failed() {
        Result passed = new Result(PASSED, 0L, null);
        Result skipped = new Result(SKIPPED, 0L, null);
        Result pending = new Result(PENDING, 0L, null);
        Result ambiguous = new Result(AMBIGUOUS, 0L, null);
        Result undefined = new Result(UNDEFINED, 0L, null);
        Result failed = new Result(FAILED, 0L, null);

        List<Result> results = asList(pending, passed, skipped, failed, ambiguous, undefined);

        sort(results, SEVERITY);

        assertThat(results, equalTo(asList(passed, skipped, pending, undefined, ambiguous, failed)));
    }

    @Test
    public void passed_result_is_always_ok() {
        Result passedResult = new Result(PASSED, 0L, null);

        assertTrue(passedResult.isOk(isStrict(false)));
        assertTrue(passedResult.isOk(isStrict(true)));
    }

    @Test
    public void skipped_result_is_always_ok() {
        Result skippedResult = new Result(SKIPPED, 0L, null);

        assertTrue(skippedResult.isOk(isStrict(false)));
        assertTrue(skippedResult.isOk(isStrict(true)));
    }

    @Test
    public void failed_result_is_never_ok() {
        Result failedResult = new Result(FAILED, 0L, null);

        assertFalse(failedResult.isOk(isStrict(false)));
        assertFalse(failedResult.isOk(isStrict(true)));
    }

    @Test
    public void undefined_result_is_only_ok_when_not_strict() {
        Result undefinedResult = new Result(UNDEFINED, 0L, null);

        assertTrue(undefinedResult.isOk(isStrict(false)));
        assertFalse(undefinedResult.isOk(isStrict(true)));
    }

    @Test
    public void pending_result_is_only_ok_when_not_strict() {
        Result pendingResult = new Result(PENDING, 0L, null);

        assertTrue(pendingResult.isOk(isStrict(false)));
        assertFalse(pendingResult.isOk(isStrict(true)));
    }

    @Test
    public void is_query_returns_true_for_the_status_of_the_result_object() {
        int checkCount = 0;
        for (Result.Type status : Result.Type.values()) {
            Result result = new Result(status, 0L, null);

            assertTrue(result.is(result.getStatus()));
            checkCount += 1;
        }
        assertTrue("No checks performed", checkCount > 0);
    }

    @Test
    public void is_query_returns_false_for_statuses_different_from_the_status_of_the_result_object() {
        int checkCount = 0;
        for (Result.Type resultStatus : Result.Type.values()) {
            Result result = new Result(resultStatus, 0L, null);
            for (Result.Type status : Result.Type.values()) {
                if (status != resultStatus) {
                    assertFalse(result.is(status));
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
