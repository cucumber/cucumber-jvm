package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;

class EmptyEngineExecutionListener implements EngineExecutionListener {

    @Override
    public void dynamicTestRegistered(TestDescriptor testDescriptor) {

    }

    @Override
    public void executionSkipped(TestDescriptor testDescriptor, String reason) {

    }

    @Override
    public void executionStarted(TestDescriptor testDescriptor) {

    }

    @Override
    public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {

    }

    @Override
    public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {

    }

}
