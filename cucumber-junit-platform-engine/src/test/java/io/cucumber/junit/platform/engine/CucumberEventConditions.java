package io.cucumber.junit.platform.engine;

import org.assertj.core.api.Condition;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.testkit.engine.Event;
import org.junit.platform.testkit.engine.EventConditions;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.allOf;
import static org.junit.platform.commons.util.FunctionUtils.where;
import static org.junit.platform.testkit.engine.Event.byTestDescriptor;
import static org.junit.platform.testkit.engine.EventConditions.displayName;
import static org.junit.platform.testkit.engine.EventConditions.uniqueIdSubstring;

class CucumberEventConditions {

    static Condition<Event> engine(Condition<Event> condition) {
        return allOf(EventConditions.engine(), condition);
    }

    static Condition<Event> examples() {
        return new Condition<>(
            byTestDescriptor(where(TestDescriptor::getUniqueId, lastSegmentTYpe("examples"))),
            "examples descriptor");
    }

    static Condition<? super Event> example(String uniqueIdSubstring, String displayName) {
        return allOf(example(), uniqueIdSubstring(uniqueIdSubstring), displayName(displayName));
    }

    static Condition<? super Event> examples(String uniqueIdSubstring, String displayName) {
        return allOf(examples(), uniqueIdSubstring(uniqueIdSubstring), displayName(displayName));
    }

    static Condition<Event> feature() {
        return new Condition<>(
            byTestDescriptor(where(TestDescriptor::getUniqueId, lastSegmentTYpe("feature"))),
            "feature descriptor");
    }

    static Condition<Event> feature(String uniqueIdSubstring, String displayName) {
        return allOf(feature(), uniqueIdSubstring(uniqueIdSubstring), displayName(displayName));
    }

    static Condition<Event> feature(String uniqueIdSubstring) {
        return allOf(feature(), uniqueIdSubstring(uniqueIdSubstring));
    }

    static Condition<Event> rule() {
        return new Condition<>(
            byTestDescriptor(where(TestDescriptor::getUniqueId, lastSegmentTYpe("rule"))),
            "rule descriptor");
    }

    static Condition<? super Event> rule(String uniqueIdSubstring, String displayName) {
        return allOf(rule(), uniqueIdSubstring(uniqueIdSubstring), displayName(displayName));
    }

    static Condition<Event> scenario() {
        return new Condition<>(
            byTestDescriptor(where(TestDescriptor::getUniqueId, lastSegmentTYpe("scenario"))),
            "feature descriptor");
    }

    static Condition<? super Event> scenario(String uniqueIdSubstring, String displayName) {
        return allOf(scenario(), uniqueIdSubstring(uniqueIdSubstring), displayName(displayName));
    }

    static Condition<? super Event> scenario(Condition<? super Event> condition) {
        return allOf(scenario(), condition);
    }

    static Condition<? super Event> scenario(String uniqueIdSubstring) {
        return allOf(scenario(), uniqueIdSubstring(uniqueIdSubstring));
    }

    static Condition<Event> source(TestSource testSource) {
        return new Condition<>(event -> event.getTestDescriptor().getSource().filter(testSource::equals).isPresent(),
            "test engine with test source '%s'", testSource);
    }

    static Condition<Event> emptySource() {
        return new Condition<>(event -> !event.getTestDescriptor().getSource().isPresent(), "without a test source");
    }

    static Condition<Event> example() {
        return new Condition<>(
            byTestDescriptor(where(TestDescriptor::getUniqueId, lastSegmentTYpe("example"))),
            "examples descriptor");
    }

    private static Predicate<UniqueId> lastSegmentTYpe(String type) {
        return uniqueId -> uniqueId.getLastSegment().getType().equals(type);
    }

    static Condition<Event> prefix(UniqueId uniqueId) {
        return new Condition<>(
            byTestDescriptor(where(TestDescriptor::getUniqueId, candidate -> candidate.hasPrefix(uniqueId))),
            "test descriptor with prefix " + uniqueId);
    }
}
