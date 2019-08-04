package io.cucumber.core.feature;

import io.cucumber.core.io.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EncodingTest {

    @Mock
    private Resource resource;

    @Test
    public void test_utf8_bom_encode() throws RuntimeException, IOException {
        when(resource.getInputStream()).thenReturn(new FileInputStream("src/test/resources/io/cucumber/core/feature/UTF_8_BOM_Encoded.feature"));
        assertFalse("UTF-8 BOM encoding not removed.", Encoding.readFile(resource).startsWith("\uFEFF"));
    }

    @Test
    public void test_utf8_encode() throws RuntimeException, IOException {
        when(resource.getInputStream()).thenReturn(new FileInputStream("src/test/resources/io/cucumber/core/feature/UTF_8_Encoded.feature"));
        assertFalse("UTF-8 BOM encoding should not be present.", Encoding.readFile(resource).startsWith("\uFEFF"));
    }

}
