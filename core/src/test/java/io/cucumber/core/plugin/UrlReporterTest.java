package io.cucumber.core.plugin;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class UrlReporterTest {
    @Test
    void printsTheCorrespondingReportsCucumberIoUrl() throws MalformedURLException {
        StringWriter out = new StringWriter();
        UrlReporter urlReporter = new UrlReporter(out);

        urlReporter.report(new URL(
            " https://cucumber-messages-app-s3bucket-1rakuy67mtnt0.s3.eu-west-3.amazonaws.com/reports/f318d9ec-5a3d-4727-adec-bd7b69e2edd3?Content-Type=application%2Fx-ndjson&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=ASIAWE4HTFKN4MW3AWBG%2F20200725%2Feu-west-3%2Fs3%2Faws4_request&X-Amz-Date=20200725T140414Z&X-Amz-Expires=900&X-Amz-Security-Token=IQoJb3JpZ2luX2VjEO7%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FwEaCWV1LXdlc3QtMyJGMEQCICDAcHT%2FjpJJUT%2BkmXN4koapI3bqENeGyNm0YCz12uB3AiBgdgeOEkPk5zIRNikqNwRoBrQtbE7lTOMRqt4H9m398yr8AQin%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F8BEAEaDDQyMjgwMTc3OTM1NSIMI%2FW7LOVE%2Bd4LvSpcKtABFHeKYLXuHkmqfWRgcOfQ%2FMujzgNhhyChkMXlEZ24ahmzev9g9HKeU9hYHIVC5k7PylOmMo%2F13tKe%2B2GPVE1oBqMQ96jeIuVfOtkfIxfMztOiQxH2RMdi2A9oWvXEGZAFDBsVmgudx7Wv9llfLpvKHcI6NKoNXxKodCk1rbhQUYO9AvyxUjhlFpFti2qCzMPEZIGe1smoIfu8nTH9HXZ3QdoWpRs7637EDEvUWirFLcvxhkg5Waay44F8jgxYBueIV3QOkRjxmlclRmOAd7jSyjDe9%2FD4BTrhAQy53BOVp3A8KqDYjIRWOhXXMJnL1%2FT2RiWcFjnxL5K8LS9AGdW6AUbOloMuHO7UlRVQUqLoaz8XFTPiR6H25ZcIEPp%2BYhFPqvtW%2FcGtzCAVT67epRizH5vKrzb5CNE7MsYiSqqJqGmtVqD1l83ig9ZTPlW0Fs%2FxxdhnPTK%2BKmrmvMheV87G2vD%2FPJdUltezKl8t2PSKgFaP79PsrcvI20C8bUQHSB5AAjAuVOzHqAi7ZiLAfpBasxLmZ97tNmuOQwPWBGrd%2B%2FeNmtUCb33fvykWG4lb0Vs48RUZYTSOz3ey%2Bg%3D%3D&X-Amz-Signature=c59735a1282954864c007d3a2136ff790d1915f7c0d2a96697cf2c073613374d&X-Amz-SignedHeaders=host"));

        String expected = String.join("\n", new String[] {
                "\u001B[36m┌──────────────────────────────────────────────────────────────────────────┐\u001B[0m",
                "\u001B[36m│\u001B[0m View your Cucumber Report at:                                            \u001B[36m│\u001B[0m",
                "\u001B[36m│\u001B[0m https://reports.cucumber.io/reports/f318d9ec-5a3d-4727-adec-bd7b69e2edd3 \u001B[36m│\u001B[0m",
                "\u001B[36m└──────────────────────────────────────────────────────────────────────────┘\u001B[0m",
                ""
        });
        assertEquals(expected, out.toString());
    }
}
