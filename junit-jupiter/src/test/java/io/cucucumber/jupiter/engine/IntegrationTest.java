package io.cucucumber.jupiter.engine;

import org.junit.platform.console.ConsoleLauncher;

public class IntegrationTest {

    public static void main(String... args) {
        ConsoleLauncher.main(
            "--exclude-engine=junit-jupiter",
            "--select-directory", "junit-jupiter/src/test/resources/io/cucumber/jupiter/engine/"
        );
    }

}
