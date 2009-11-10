package cuke4duke.internal.jvmclass;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import cuke4duke.StepMother;
import cuke4duke.internal.java.JavaAnalyzer;
import cuke4duke.internal.language.Transformable;

public class OverrideDefaultTransformationsTest {
    private Map<Class<?>, Transformable> transforms;
    private ArgumentsConverter converter;

    @Before
    public void setUp() throws Throwable {
        System.setProperty("cuke4duke.objectFactory", "cuke4duke.internal.jvmclass.PicoFactory");
        ClassLanguage classLanguage = new ClassLanguage(mock(ClassLanguageMixin.class), mock(StepMother.class), Arrays.asList(new ClassAnalyzer[] { new JavaAnalyzer() }));
        classLanguage.load_code_file("cuke4duke/internal/jvmclass/MyTransforms.class");
        classLanguage.prepareScenario();
        transforms = classLanguage.getTransforms();
        converter = new ArgumentsConverter(transforms);
    }

    @Test
    public void shouldConvertFromStringToBooleanPrimitive() {
        Object[] convertedObject = converter
                .convert(new Class<?>[] { Boolean.TYPE }, new Object[] { "yes" });
        assertTrue(convertedObject[0].getClass().isAssignableFrom(Boolean.class));
        assertTrue((Boolean) convertedObject[0]);
    }
}
