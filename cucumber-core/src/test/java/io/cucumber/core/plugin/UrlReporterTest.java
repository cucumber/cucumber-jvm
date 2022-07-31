package io.cucumber.core.plugin;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static io.cucumber.core.plugin.BytesEqualTo.isBytesEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;

class UrlReporterTest {
    final String message = "\u001B[32m\u001B[1m┌──────────────────────────────────────────────────────────────────────────┐\u001B[0m\n"
            +
            "\u001B[32m\u001B[1m│\u001B[0m View your Cucumber Report at:                                            \u001B[32m\u001B[1m│\u001B[0m\n"
            +
            "\u001B[32m\u001B[1m│\u001B[0m \u001B[36m\u001B[1m\u001B[4mhttps://reports.cucumber.io/reports/f318d9ec-5a3d-4727-adec-bd7b69e2edd3\u001B[0m \u001B[32m\u001B[1m│\u001B[0m\n"
            +
            "\u001B[32m\u001B[1m│\u001B[0m                                                                          \u001B[32m\u001B[1m│\u001B[0m\n"
            +
            "\u001B[32m\u001B[1m│\u001B[0m This report will self-destruct in 24h unless it is claimed or deleted.   \u001B[32m\u001B[1m│\u001B[0m\n"
            +
            "\u001B[32m\u001B[1m└──────────────────────────────────────────────────────────────────────────┘\u001B[0m\n";

    @Test
    void printsTheCorrespondingReportsCucumberIoUrl() throws UnsupportedEncodingException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        UrlReporter urlReporter = new UrlReporter(new PrintStream(bytes, false, StandardCharsets.UTF_8.name()));
        urlReporter.report(message);
        assertThat(bytes, isBytesEqualTo(message));
    }

    @Test
    void printsTheCorrespondingReportsCucumberIoUrlInMonoChrome() throws UnsupportedEncodingException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        UrlReporter urlReporter = new UrlReporter(new PrintStream(bytes, false, StandardCharsets.UTF_8.name()));
        urlReporter.setMonochrome(true);

        urlReporter.report(message);
        assertThat(bytes, isBytesEqualTo("" +
                "┌──────────────────────────────────────────────────────────────────────────┐\n" +
                "│ View your Cucumber Report at:                                            │\n" +
                "│ https://reports.cucumber.io/reports/f318d9ec-5a3d-4727-adec-bd7b69e2edd3 │\n" +
                "│                                                                          │\n" +
                "│ This report will self-destruct in 24h unless it is claimed or deleted.   │\n" +
                "└──────────────────────────────────────────────────────────────────────────┘\n"));
    }

}
