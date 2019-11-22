package io.cucumber.junit.platform.engine;

import org.junit.platform.console.ConsoleLauncher;

public class ConsoleLauncherDemonstration {

    public static void main(String... args) {
        ConsoleLauncher.main(
            "--exclude-engine=junit-jupiter",
            "--config=cucumber.execution.strict=true",
//            "--select-directory", "junit-platform-engine/src/test/resources/io/cucumber/junit/platform/engine/"
            "--exclude-package=io.cucumber.junit.platform.engine.test",
            "--select-package=io"

        );
    }

}
