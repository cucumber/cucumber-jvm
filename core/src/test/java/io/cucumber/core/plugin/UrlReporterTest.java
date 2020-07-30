package io.cucumber.core.plugin;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class UrlReporterTest {

    @Test
    void printsTheCorrespondingReportsCucumberIoUrl() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        UrlReporter urlReporter = new UrlReporter(new PrintStream(bytes));

        urlReporter.report(new URL(
            "https://messages.example.com/reports/f318d9ec-5a3d-4727-adec-bd7b69e2edd3?q=example"));

        assertThat(bytes.toString("UTF-8"), is("" +
                "\u001B[32m\u001B[1m┌──────────────────────────────────────────────────────────────────────────┐\u001B[0m\n" +
                "\u001B[32m\u001B[1m│\u001B[0m View your Cucumber Report at:                                            \u001B[32m\u001B[1m│\u001B[0m\n" +
                "\u001B[32m\u001B[1m│\u001B[0m \u001B[36m\u001B[1mhttps://reports.cucumber.io/reports/f318d9ec-5a3d-4727-adec-bd7b69e2edd3\u001B[0m \u001B[32m\u001B[1m│\u001B[0m\n" +
                "\u001B[32m\u001B[1m│\u001B[0m                                                                          \u001B[32m\u001B[1m│\u001B[0m\n" +
                "\u001B[32m\u001B[1m│\u001B[0m This report will self-destruct in 12h unless it is claimed or deleted.   \u001B[32m\u001B[1m│\u001B[0m\n" +
                "\u001B[32m\u001B[1m└──────────────────────────────────────────────────────────────────────────┘\u001B[0m\n"));
    }

    @Test
    void printsTheCorrespondingReportsCucumberIoUrlInMonoChrome() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        UrlReporter urlReporter = new UrlReporter(new PrintStream(bytes));
        urlReporter.setMonochrome(true);

        urlReporter.report(new URL(
            "https://messages.example.com/reports/f318d9ec-5a3d-4727-adec-bd7b69e2edd3?q=example"));

        assertThat(bytes.toString("UTF-8"), is("" +
                "┌──────────────────────────────────────────────────────────────────────────┐\n" +
                "│ View your Cucumber Report at:                                            │\n" +
                "│ https://reports.cucumber.io/reports/f318d9ec-5a3d-4727-adec-bd7b69e2edd3 │\n" +
                "│                                                                          │\n" +
                "│ This report will self-destruct in 12h unless it is claimed or deleted.   │\n" +
                "└──────────────────────────────────────────────────────────────────────────┘\n"));
    }

}
