package io.cucumber.core.plugin;

import org.junit.jupiter.api.Test;

class NoPublishFormatterTest {
    @Test
    public void eyeBallTest() {
        new NoPublishFormatter("true").printBanner();
    }
}
