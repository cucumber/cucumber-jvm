package cucumber.runtime.java.spring;

import java.io.PrintWriter;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.asm.ClassReader;
import org.mockito.asm.ClassWriter;
import org.mockito.asm.Type;
import org.mockito.asm.util.ASMifierClassVisitor;
import org.springframework.test.context.ContextConfiguration;

public class GlueScopeConfigEnhancerTest {

	@Test
	public void stepWithContextConfigurationAnnotationAndValue_enhance_GlueScopeConfigAdded() {
		GlueScopeConfigEnhancer underTest = new GlueScopeConfigEnhancer();

		Class enhanced = underTest.enhance(StepWithContexttConfigurationAnnotationAndValue.class);

		Assert.assertNotNull(enhanced);

		ContextConfiguration annotation = (ContextConfiguration) enhanced.getAnnotation(ContextConfiguration.class);

		Assert.assertNotNull(annotation);
		Assert.assertEquals(GlueScopeConfig.class, annotation.classes()[annotation.classes().length-1]);
	}

	
	@ContextConfiguration(classes=Object.class)
	public static class StepWithContexttConfigurationAnnotationAndValue {
	}


}
