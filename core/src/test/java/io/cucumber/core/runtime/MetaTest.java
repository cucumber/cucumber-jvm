package io.cucumber.core.runtime;

import io.cucumber.messages.Messages;
import org.junit.jupiter.api.Test;

import static io.cucumber.core.runtime.Meta.makeMeta;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.core.Is.is;

public class MetaTest {
    @Test
    public void generates_a_meta_message() {
        Runtime runtime = Runtime.builder().build();
        Messages.Meta meta = makeMeta();
        assertThat(meta.getProtocolVersion(), matchesPattern("\\d+\\.\\d+\\.\\d+"));
        assertThat(meta.getImplementation().getName(), is("cucumber-jvm"));
        assertThat(meta.getImplementation().getVersion(), is("unreleased"));
        assertThat(meta.getOs().getName(), matchesPattern(".+"));
        assertThat(meta.getCpu().getName(), matchesPattern(".+"));
    }
}
