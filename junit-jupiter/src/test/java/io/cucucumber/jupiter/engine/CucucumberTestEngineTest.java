package io.cucucumber.jupiter.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CucucumberTestEngineTest {

    private final CucucumberTestEngine engine = new CucucumberTestEngine();

    @Test
    void id() {
        assertEquals("cucumber", engine.getId());
    }

    @Test
    void groupId() {
        assertEquals("io.cucucumber", engine.getGroupId().get());
    }

    @Test
    void artifactId() {
        assertEquals("cucucumber-junit-jupiter", engine.getArtifactId().get());
    }

    @Test
    void version() {
        assertEquals("DEVELOPMENT", engine.getVersion().get());
    }

}
