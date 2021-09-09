package io.cucumber.core.plugin;

import io.cucumber.core.resource.Resource;
import io.cucumber.plugin.event.TestSourceRead;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import static java.nio.charset.StandardCharsets.UTF_8;

final class TestSourceReadResource implements Resource {

    private final TestSourceRead event;

    TestSourceReadResource(TestSourceRead event) {
        this.event = event;
    }

    @Override
    public URI getUri() {
        return event.getUri();
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(event.getSource().getBytes(UTF_8));
    }

}
