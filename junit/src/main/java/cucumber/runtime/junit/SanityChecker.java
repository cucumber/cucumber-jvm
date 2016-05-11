package cucumber.runtime.junit;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.RunListener;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Listener that makes sure Cucumber fires events in the right order
 */
public class SanityChecker extends RunListener {
    private static final String INDENT = "  ";
    public static final String INSANITY = "INSANITY";

    private List<Description> testDescriptions = new ArrayList<Description>();
    private final StringWriter out = new StringWriter();

    public static void run(Class<?> testClass) {
        run(testClass, false);
    }

    public static void run(Class<?> testClass, boolean debug) {
        JUnitCore runner = new JUnitCore();
        SanityChecker listener = new SanityChecker();
        runner.addListener(listener);
        runner.run(testClass);
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
    public void testStarted(Description started) {
        spaces();
        out.append("START  " + started.toString()).append("\n");
        testDescriptions.add(started);
    }

    @Override
    public void testFinished(Description ended) {
        try {
            Description lastStarted = testDescriptions.remove(testDescriptions.size() - 1);
            spaces();
            out.append("END    " + ended.toString()).append("\n");
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

    @Override
    public void testIgnored(Description ignored) {
        try {
            Description lastStarted = testDescriptions.remove(testDescriptions.size() - 1);
            spaces();
            out.append("IGNORE " + ignored.toString()).append("\n");
            // For suites both a testIgnored and a testFinished is sent
            if (ignored.isSuite()) {
                testDescriptions.add(lastStarted);
            }
            if (!lastStarted.toString().equals(ignored.toString())) {
                out.append(INSANITY).append("\n");
                String errorMessage = String.format("Started : %s\nIgnored : %s\n", lastStarted, ignored);
                out.append(errorMessage).append("\n");
            }
        } catch (Exception e) {
            out.append(INSANITY).append("\n");
            e.printStackTrace(new PrintWriter(out));
        }
    }

    private void spaces() {
        for (int i = 0; i < testDescriptions.size(); i++) {
            out.append(INDENT);
        }
    }

    public String getOutput() {
        return out.toString();
    }
}