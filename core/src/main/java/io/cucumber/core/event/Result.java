package io.cucumber.core.event;

import java.time.Duration;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class Result{

    private final Status status;
    private final Duration duration;
    private final Throwable error;

    /**
     * The result of a step or scenario
     *
     * @param status status of the step or scenario
     * @param duration the duration
     * @param error the error that caused the failure if any
     */
    public Result(Status status, Duration duration, Throwable error) {
        this.status = requireNonNull(status);
        this.duration = requireNonNull(duration);
        this.error = error;
    }

    public Status getStatus() {
        return status;
    }

    public Duration getDuration() {
        return duration;
    }

    public Throwable getError() {
        return error;
    }

    @Override
    public String toString() {
        return "Result{" +
            "status=" + status +
            ", duration=" + duration.getSeconds() +
            ", error=" + error +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result result = (Result) o;
        return status == result.status &&
            Objects.equals(duration, result.duration) &&
            Objects.equals(error, result.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, duration, error);
    }
}
