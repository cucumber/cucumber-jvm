package cucumber.api;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

public class Result {
    private static final long serialVersionUID = 1L;

    private final String status;
    private final Long duration;
    private final Throwable error;
    private final List<String> snippets;
    public static final Result SKIPPED = new Result("skipped", null, null);
    public static final String UNDEFINED = "undefined";
    public static final String PASSED = "passed";
    public static final String PENDING = "pending";
    public static final String FAILED = "failed";

    /**
     * Used at runtime
     *
     * @param status
     * @param duration
     * @param error
     */
    public Result(String status, Long duration, Throwable error) {
        this(status, duration, error, Collections.<String>emptyList());
    }

    /**
     * Used at runtime
     *
     * @param status
     * @param duration
     * @param error
     * @param snippets
     */
    public Result(String status, Long duration, Throwable error, List<String> snippets) {
        this.status = status;
        this.duration = duration;
        this.error = error;
        this.snippets = snippets;
    }

    public String getStatus() {
        return status;
    }

    public Long getDuration() {
        return duration;
    }

    public String getErrorMessage() {
        return error != null ? getErrorMessage(error) : null;
    }

    public Throwable getError() {
        return error;
    }

    public List<String> getSnippets() {
        return snippets;
    }

    public boolean isOk(boolean isStrict) {
        return hasAlwaysOkStatus() || !isStrict && hasOkWhenNotStrictStatus();
    }

    private boolean hasAlwaysOkStatus() {
        return Result.PASSED.equals(status) || Result.SKIPPED.getStatus().equals(status);
    }

    private boolean hasOkWhenNotStrictStatus() {
        return Result.UNDEFINED.equals(status) || Result.PENDING.equals(status);
    }

    private String getErrorMessage(Throwable error) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        error.printStackTrace(printWriter);
        return stringWriter.getBuffer().toString();
    }
}
