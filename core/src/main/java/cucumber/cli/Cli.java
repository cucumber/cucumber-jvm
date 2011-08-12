package cucumber.cli;

import cucumber.io.FileResource;
import cucumber.junit.RunnerBuilder;
import cucumber.junit.ScenarioRunner;
import cucumber.runtime.Runtime;
import gherkin.GherkinParser;
import org.junit.runner.notification.RunNotifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Cli {
    private final Runtime runtime;
    private final List<String> filesOrDirs;
    private List<ScenarioRunner> scenarioRunners = new ArrayList<ScenarioRunner>();

    public Cli(Runtime runtime, List<String> filesOrDirs) {
        this.runtime = runtime;
        this.filesOrDirs = filesOrDirs;
    }

    public void run() {
        // Load all gherkin files
        traverse(filesOrDirs.toArray(new String[filesOrDirs.size()]));

        RunNotifier notifier = new RunNotifier();
        notifier.addListener(new CucumberRunListener());

        for (ScenarioRunner scenarioRunner : scenarioRunners) {
            scenarioRunner.run(notifier);
        }
    }

    private void traverse(String[] filesOrDirs) {
        for (String fileOrDir : filesOrDirs) {
            traverse(new File(fileOrDir));
        }
    }

    private void traverse(File fileOrDir) {
        if (fileOrDir.isDirectory()) {
            File[] files = fileOrDir.listFiles();
            for (File file : files) {
                traverse(file);
            }
        } else if (fileOrDir.isFile() && fileOrDir.getName().endsWith(".feature")) {
            addFeature(fileOrDir);
        }
    }

    private void addFeature(File path) {
        System.out.println("path = " + path);
        RunnerBuilder builder = new RunnerBuilder(runtime, scenarioRunners);
        GherkinParser gherkinParser = new GherkinParser(builder);
        String source = new FileResource(null, path).getString();
        gherkinParser.parse(source, path.getAbsolutePath(), 0);
    }
}
