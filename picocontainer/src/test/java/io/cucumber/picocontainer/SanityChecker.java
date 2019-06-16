package io.cucumber.picocontainer;

import junit.framework.AssertionFailedError;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestResult;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Listener that makes sure Cucumber fires events in the right order
 */
public class SanityChecker implements TestListener {
    private static final String INDENT = "  ";
    private static final String INSANITY = "INSANITY";

    private List<Test> tests = new ArrayList<>();
    private final StringWriter out = new StringWriter();

    public static void run(Class<?> testClass) {
        run(testClass, false);
    }

    static void run(Class<?> testClass, boolean debug) {
        JUnit4TestAdapter testAdapter = new JUnit4TestAdapter(testClass);
        TestResult result = new TestResult();
        SanityChecker listener = new SanityChecker();
        result.addListener(listener);
        testAdapter.run(result);
        String output = listener.getOutput();
        if (output.contains(INSANITY)) {
            throw new RuntimeException("Something went wrong\n" + output);
        }
        if (debug) {
            System.out.println("===== " + testClass.getName());
            System.out.println(output);
            System.out.println("=====");
        }
    }

    @Override
    public void addError(Test test, Throwable t) {
    }

    @Override
    public void addFailure(Test test, AssertionFailedError t) {
    }

    @Override
    public void startTest(Test started) {
        spaces();
        out.append("START ").append(started.toString()).append("\n");
        tests.add(started);
    }

    @Override
    public void endTest(Test ended) {
        try {
            Test lastStarted = tests.remove(tests.size() - 1);
            spaces();
            out.append("END   ").append(ended.toString()).append("\n");
            if (!lastStarted.toString().equals(ended.toString())) {
                out.append(INSANITY).append("\n");
                String errorMessage = String.format("Started : %s\nEnded   : %s\n", lastStarted, ended);
                out.append(errorMessage).append("\n");
            }
        } catch (Exception e) {
            out.append(INSANITY).append("\n");
            e.printStackTrace(new PrintWriter(out));
        }
    }

    private void spaces() {
        for (int i = 0; i < tests.size(); i++) {
            out.append(INDENT);
        }
    }

    private String getOutput() {
        return out.toString();
    }
}
