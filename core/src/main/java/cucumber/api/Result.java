package cucumber.api;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Result {
    private static final long serialVersionUID = 1L;

    private final Result.Type status;
    private final Long duration;
    private final Throwable error;
    public static final Result SKIPPED = new Result(Result.Type.SKIPPED, null, null);
    public static enum Type {
        PASSED,
        SKIPPED,
        PENDING,
        UNDEFINED,
        FAILED;

        public static Type fromLowerCaseName(String lowerCaseName) {
            return valueOf(lowerCaseName.toUpperCase());
        }

        public String lowerCaseName() {
            return name().toLowerCase();
        }

        public String firstLetterCapitalizedName() {
            return name().substring(0, 1) + name().substring(1).toLowerCase();
        }


    }

    /**
     * Used at runtime
     *
     * @param status
     * @param duration
     * @param error
     */
    public Result(Result.Type status, Long duration, Throwable error) {
        this.status = status;
        this.duration = duration;
        this.error = error;
    }

    public Result.Type getStatus() {
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

    public boolean is(Result.Type status) {
        return this.status == status;
    }

    public boolean isOk(boolean isStrict) {
        return hasAlwaysOkStatus() || !isStrict && hasOkWhenNotStrictStatus();
    }

    private boolean hasAlwaysOkStatus() {
        return is(Result.Type.PASSED) || is(Result.Type.SKIPPED);
    }

    private boolean hasOkWhenNotStrictStatus() {
        return is(Result.Type.UNDEFINED) || is(Result.Type.PENDING);
    }

    private String getErrorMessage(Throwable error) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        error.printStackTrace(printWriter);
        return stringWriter.getBuffer().toString();
    }
}
