package io.cucumber.core.plugin;

import io.cucumber.messages.LocationComparator;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.TestCaseStarted;
import io.cucumber.query.Lineage;

import java.util.Comparator;

import static java.util.Objects.requireNonNull;

class JvmElementData {
    static final Comparator<JvmElementData> comparator = Comparator
            .comparing((JvmElementData data) -> data.pickle.getUri())
            // TODO: Use location from pickle
            .thenComparing(data -> data.location, new LocationComparator());

    final TestCaseStarted testCaseStarted;
    final Lineage lineage;
    final Pickle pickle;
    final Location location;
    final TestStepData testStepData;

    JvmElementData(
            TestCaseStarted testCaseStarted, Lineage lineage, Pickle pickle, Location location,
            TestStepData testStepData
    ) {
        this.testCaseStarted = requireNonNull(testCaseStarted);
        this.lineage = requireNonNull(lineage);
        this.pickle = requireNonNull(pickle);
        this.location = requireNonNull(location);
        this.testStepData = requireNonNull(testStepData);
    }
}
