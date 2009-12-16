package cuke4duke.internal.jvmclass;

import cuke4duke.StepMother;
import cuke4duke.internal.java.JavaAnalyzer;
import cuke4duke.internal.java.MethodFormat;
import cuke4duke.internal.language.Transformable;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class OverrideDefaultTransformationsTest {
    private ArgumentsConverter converter;

    @Before
    public void setUp() throws Throwable {
        System.setProperty("cuke4duke.objectFactory", "cuke4duke.internal.jvmclass.PicoFactory");
        ClassLanguage classLanguage = new ClassLanguage(mock(ClassLanguageMixin.class), mock(StepMother.class), Arrays.<ClassAnalyzer>asList(new JavaAnalyzer()));
        classLanguage.load_code_file("cuke4duke/internal/jvmclass/MyTransforms.class");
        classLanguage.begin_scenario(null);
        Map<Class<?>, Transformable> transforms = classLanguage.getTransforms();
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
