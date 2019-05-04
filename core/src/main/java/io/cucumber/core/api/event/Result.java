package io.cucumber.core.api.event;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Comparator;
import java.util.Objects;

import static java.time.Duration.ZERO;
import static java.util.Locale.ROOT;
import static java.util.Objects.requireNonNull;

public final class Result {

    public static final Comparator<Result> SEVERITY = new Comparator<Result>() {

        @Override
        public int compare(Result a, Result b) {
            return a.status == b.status ? 0 : a.status.ordinal() > b.status.ordinal() ? 1 : -1;
        }
    };

    private final Type status;
    private final Long duration;
    private final Duration timeDuration;
    private final Throwable error;
    public static final Result UNDEFINED = new Result(Result.Type.UNDEFINED, ZERO, null);
    public enum Type {
        PASSED,
        SKIPPED,
        PENDING,
        UNDEFINED,
        AMBIGUOUS,
        FAILED;

        public static Type fromLowerCaseName(String lowerCaseName) {
            return valueOf(lowerCaseName.toUpperCase(ROOT));
        }

        public String lowerCaseName() {
            return name().toLowerCase(ROOT);
        }

        public String firstLetterCapitalizedName() {
            return name().substring(0, 1) + name().substring(1).toLowerCase(ROOT);
        }


    }

    /**
     * The result of a step or scenario
     *
     * @param status status of the step or scenario
     * @param timeDuration the duration in nanoseconds
     * @param error the error that caused the failure if any
     */
    @Deprecated
    public Result(Result.Type status, Long durationLong, Throwable error) {
        this.status = requireNonNull(status);
        this.duration = requireNonNull(durationLong);
        this.timeDuration = Duration.ZERO;
        this.error = error;
    }
    
    public Result(Result.Type status, Duration duration, Throwable error) {
        this.status = requireNonNull(status);
        this.duration = 0L;
        this.timeDuration = requireNonNull(duration);
        this.error = error;
    }

    public Result.Type getStatus() {
        return status;
    }

    @Deprecated
    public Long getDuration() {
        return duration;
    }

    public Duration getTimeDuration() {
        return timeDuration;
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

    @Override
    public String toString() {
        return "Result{" +
            "status=" + status +
            ", duration=" + timeDuration.getSeconds() +
            ", error=" + error +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result result = (Result) o;
        return status == result.status &&
            Objects.equals(timeDuration, result.timeDuration) &&
            Objects.equals(error, result.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, timeDuration, error);
    }
}
