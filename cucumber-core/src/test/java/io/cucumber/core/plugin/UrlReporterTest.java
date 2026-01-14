package io.cucumber.core.plugin;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static io.cucumber.core.plugin.Bytes.bytes;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class UrlReporterTest {
    final String message = """
            \u001B[32m\u001B[1m┌──────────────────────────────────────────────────────────────────────────┐\u001B[0m
            \u001B[32m\u001B[1m│\u001B[0m View your Cucumber Report at:                                            \u001B[32m\u001B[1m│\u001B[0m
            \u001B[32m\u001B[1m│\u001B[0m \u001B[36m\u001B[1m\u001B[4mhttps://reports.cucumber.io/reports/f318d9ec-5a3d-4727-adec-bd7b69e2edd3\u001B[0m \u001B[32m\u001B[1m│\u001B[0m
            \u001B[32m\u001B[1m│\u001B[0m                                                                          \u001B[32m\u001B[1m│\u001B[0m
            \u001B[32m\u001B[1m│\u001B[0m This report will self-destruct in 24h unless it is claimed or deleted.   \u001B[32m\u001B[1m│\u001B[0m
            \u001B[32m\u001B[1m└──────────────────────────────────────────────────────────────────────────┘\u001B[0m
            """;

    @Test
    void printsTheCorrespondingReportsCucumberIoUrl() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        UrlReporter urlReporter = new UrlReporter(new PrintStream(bytes, false, StandardCharsets.UTF_8));
        urlReporter.report(message);
        assertThat(bytes, bytes(equalTo(message)));
    }

    @Test
    void printsTheCorrespondingReportsCucumberIoUrlInMonoChrome() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        UrlReporter urlReporter = new UrlReporter(new PrintStream(bytes, false, StandardCharsets.UTF_8));
        urlReporter.setMonochrome(true);

        urlReporter.report(message);
        assertThat(bytes, bytes(equalTo("""
                ┌──────────────────────────────────────────────────────────────────────────┐
                │ View your Cucumber Report at:                                            │
                │ https://reports.cucumber.io/reports/f318d9ec-5a3d-4727-adec-bd7b69e2edd3 │
                │                                                                          │
                │ This report will self-destruct in 24h unless it is claimed or deleted.   │
                └──────────────────────────────────────────────────────────────────────────┘
                """)));
    }

}
