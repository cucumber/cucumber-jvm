package cucumber.api;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ResultTest {

    @Test
    public void passed_result_is_always_ok() {
        Result passedResult = new Result(Result.PASSED, null, null);

        assertTrue(passedResult.isOk(isStrict(false)));
        assertTrue(passedResult.isOk(isStrict(true)));
    }

    @Test
    public void skipped_result_is_always_ok() {
        assertTrue(Result.SKIPPED.isOk(isStrict(false)));
        assertTrue(Result.SKIPPED.isOk(isStrict(true)));
    }

    @Test
    public void failed_result_is_never_ok() {
        Result failedResult = new Result(Result.FAILED, null, null);

        assertFalse(failedResult.isOk(isStrict(false)));
        assertFalse(failedResult.isOk(isStrict(true)));
    }

    @Test
    public void undefined_result_is_only_ok_when_not_strict() {
        Result undefinedResult = new Result(Result.UNDEFINED, null, null);

        assertTrue(undefinedResult.isOk(isStrict(false)));
        assertFalse(undefinedResult.isOk(isStrict(true)));
    }

    @Test
    public void pending_result_is_only_ok_when_not_strict() {
        Result pendingResult = new Result(Result.PENDING, null, null);

        assertTrue(pendingResult.isOk(isStrict(false)));
        assertFalse(pendingResult.isOk(isStrict(true)));
    }

    private boolean isStrict(boolean value) {
        return value;
    }
}
