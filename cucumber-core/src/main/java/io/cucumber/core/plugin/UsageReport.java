package io.cucumber.core.plugin;

import java.time.Duration;
import java.util.List;

import static java.util.Objects.requireNonNull;

final class UsageReport {
    private final List<StepDefinitionUsage> stepDefinitions;

    UsageReport(List<StepDefinitionUsage> stepDefinitions) {
        this.stepDefinitions = requireNonNull(stepDefinitions);
    }

    public List<StepDefinitionUsage> getStepDefinitions() {
        return stepDefinitions;
    }

    /**
     * Container for usage-entries of steps
     */
    static final class StepDefinitionUsage {

        private final String expression;
        private final String location;
        private final Statistics duration;
        private final List<StepUsage> steps;

        StepDefinitionUsage(
                String expression, String location, Statistics duration, List<StepUsage> steps
        ) {
            this.expression = requireNonNull(expression);
            this.location = requireNonNull(location);
            this.duration = duration;
            this.steps = requireNonNull(steps);
        }

        public String getExpression() {
            return expression;
        }

        public Statistics getDuration() {
            return duration;
        }

        public List<StepUsage> getSteps() {
            return steps;
        }

        public String getLocation() {
            return location;
        }
    }

    static final class Statistics {
        private final Duration sum;
        private final Duration mean;
        private final Duration moe95;

        Statistics(Duration sum, Duration mean, Duration moe95) {
            this.sum = sum;
            this.mean = mean;
            this.moe95 = moe95;
        }

        public Duration getSum() {
            return sum;
        }

        public Duration getMean() {
            return mean;
        }

        public Duration getMoe95() {
            return moe95;
        }
    }

    static final class StepUsage {

        private final String text;
        private final Duration duration;
        private final String location;

        StepUsage(String text, Duration duration, String location) {
            this.text = requireNonNull(text);
            this.duration = requireNonNull(duration);
            this.location = requireNonNull(location);
        }

        public Duration getDuration() {
            return duration;
        }

        public String getLocation() {
            return location;
        }

        public String getText() {
            return text;
        }
    }
}
