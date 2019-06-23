package io.cucumber.core.feature;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.IOException;

import io.cucumber.core.io.Resource;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class EncodingTest {
	
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    
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