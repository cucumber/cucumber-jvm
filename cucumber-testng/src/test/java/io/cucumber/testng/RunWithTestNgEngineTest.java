package io.cucumber.testng;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.junit.platform.testkit.engine.EventConditions.container;
import static org.junit.platform.testkit.engine.EventConditions.displayName;
import static org.junit.platform.testkit.engine.EventConditions.dynamicTestRegistered;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.legacyReportingName;
import static org.junit.platform.testkit.engine.EventConditions.started;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.EventConditions.uniqueIdSubstring;

class RunWithTestNgEngineTest {

    @Test
    void discover() {
        var request = request()
                .selectors(selectClass(RunFeatureWithThreeScenariosTest.class))
                .build();
        var results = EngineTestKit.discover("testng", request);
        var descendants = results.getEngineDescriptor().getDescendants();

        assertThat(descendants)
                .extracting(TestDescriptor::getDisplayName)
                .containsExactly(
                    "RunFeatureWithThreeScenariosTest",
                    "runScenario(io.cucumber.testng.PickleWrapper, io.cucumber.testng.FeatureWrapper)");

    }

    @Test
    void execute() {
        var request = request()
                .selectors(selectClass(RunFeatureWithThreeScenariosTest.class))
                .build();
        var results = EngineTestKit.execute("testng", request);

        String signature = "runScenario(io.cucumber.testng.PickleWrapper, io.cucumber.testng.FeatureWrapper)";
        results.allEvents().assertEventsMatchLooselyInOrder( //
            event(testClass(RunFeatureWithThreeScenariosTest.class), started()), //
            event(container("method:" + signature), started()), //
            event(uniqueIdSubstring("method:" + signature), dynamicTestRegistered("invoc:0"),
                displayName("[0] \"SC1\", \"A feature containing 3 scenarios\"")), //
            event(uniqueIdSubstring("method:" + signature), test("invoc:0"), started()), //
            event(uniqueIdSubstring("method:" + signature), test("invoc:0"), finishedSuccessfully()), //
            event(uniqueIdSubstring("method:" + signature), dynamicTestRegistered("invoc:1"),
                displayName("[1] \"SC2\", \"A feature containing 3 scenarios\"")), //
            event(uniqueIdSubstring("method:" + signature), test("invoc:1"), started()), //
            event(uniqueIdSubstring("method:" + signature), test("invoc:1"), finishedSuccessfully()), //
            event(uniqueIdSubstring("method:" + signature), dynamicTestRegistered("invoc:2"),
                displayName("[2] \"SC3\", \"A feature containing 3 scenarios\"")), //
            event(uniqueIdSubstring("method:" + signature), test("invoc:2"), started()), //
            event(uniqueIdSubstring("method:" + signature), test("invoc:2"), finishedSuccessfully()), //
            event(container("method:" + signature), finishedSuccessfully()), //
            event(testClass(RunFeatureWithThreeScenariosTest.class), finishedSuccessfully()));

    }

    static Condition<Event> testClass(Class<?> testClass) {
        return container(event(displayName(testClass.getSimpleName()), uniqueIdSubstring(testClass.getName()),
            legacyReportingName(testClass.getName())));
    }
}
