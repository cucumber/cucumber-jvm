package io.cucucumber.jupiter.engine;

import org.junit.platform.console.ConsoleLauncher;

public class ConsoleLauncherDemonstration {

    public static void main(String... args) {
        ConsoleLauncher.main(
            "--exclude-engine=junit-jupiter",
            "--config=cucumber.execution.strict=false",
//            "--select-directory", "junit-platform-engine/src/test/resources/io/cucumber/jupiter/engine/"
//            "--exclude-package=io.cucumber.jupiter.engine.test",
            "--select-package=io"

        );
    }

}
