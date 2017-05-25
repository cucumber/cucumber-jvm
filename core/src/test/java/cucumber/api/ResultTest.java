package cucumber.api;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ResultTest {

    @Test
    public void passed_result_is_always_ok() {
        Result passedResult = new Result(Result.Type.PASSED, null, null);

        assertTrue(passedResult.isOk(isStrict(false)));
        assertTrue(passedResult.isOk(isStrict(true)));
    }

    @Test
    public void skipped_result_is_always_ok() {
        Result skippedResult = new Result(Result.Type.SKIPPED, null, null);

        assertTrue(skippedResult.isOk(isStrict(false)));
        assertTrue(skippedResult.isOk(isStrict(true)));
    }

    @Test
    public void failed_result_is_never_ok() {
        Result failedResult = new Result(Result.Type.FAILED, null, null);

        assertFalse(failedResult.isOk(isStrict(false)));
        assertFalse(failedResult.isOk(isStrict(true)));
    }

    @Test
    public void undefined_result_is_only_ok_when_not_strict() {
        Result undefinedResult = new Result(Result.Type.UNDEFINED, null, null);

        assertTrue(undefinedResult.isOk(isStrict(false)));
        assertFalse(undefinedResult.isOk(isStrict(true)));
    }

    @Test
    public void pending_result_is_only_ok_when_not_strict() {
        Result pendingResult = new Result(Result.Type.PENDING, null, null);

        assertTrue(pendingResult.isOk(isStrict(false)));
        assertFalse(pendingResult.isOk(isStrict(true)));
    }

    @Test
    public void is_query_returns_true_for_the_status_of_the_result_object() {
        int checkCount = 0;
        for (Result.Type status : Result.Type.values()) {
            Result result = new Result(status, null, null);

            assertTrue(result.is(result.getStatus()));
            checkCount += 1;
        }
        assertTrue("No checks performed", checkCount > 0);
    }

    @Test
    public void is_query_returns_false_for_statuses_different_from_the_status_of_the_result_object() {
        int checkCount = 0;
        for (Result.Type resultStatus : Result.Type.values()) {
            Result result = new Result(resultStatus, null, null);
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
