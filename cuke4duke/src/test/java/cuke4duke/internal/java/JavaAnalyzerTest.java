package cuke4duke.internal.java;

import cuke4duke.Transform;
import cuke4duke.internal.jvmclass.ClassLanguage;
import cuke4duke.internal.jvmclass.ObjectFactory;
import cuke4duke.internal.language.Transformable;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class JavaAnalyzerTest {

    private JavaAnalyzer javaAnalyzer;
    private ClassWithTransformer transformer;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private ClassLanguage classLanguage;

    public JavaAnalyzerTest() {
        initMocks(this);
        this.javaAnalyzer = new JavaAnalyzer();
        this.transformer = new ClassWithTransformer();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldAddTransformToClassLanguage() throws Throwable {
        when(classLanguage.getClasses()).thenReturn(Collections.<Class<?>>singletonList(ClassWithTransformer.class));
        javaAnalyzer.populateStepDefinitionsAndHooks(objectFactory, classLanguage);

        ArgumentCaptor<Transformable> trasnformableArgument = ArgumentCaptor.forClass(Transformable.class);
        ArgumentCaptor<Class> returnTypeArgument = ArgumentCaptor.forClass(Class.class);
        verify(classLanguage).addTransform(returnTypeArgument.capture(), trasnformableArgument.capture());
        
        Class returnType = returnTypeArgument.getValue();
        Transformable transform = trasnformableArgument.getValue();
        
        assertTrue(returnType.isAssignableFrom(Integer.TYPE));

        for (Field field : transform.getClass().getDeclaredFields()) {
            if (field.getDeclaringClass().isAssignableFrom(Method.class)) {
                field.setAccessible(true);
                assertEquals(((Method) field.get(transform)).getName(), transformer.getClass().getDeclaredMethods()[0]);
            }
        }
    }
    
    private class ClassWithTransformer {

        @SuppressWarnings("unused")
        @Transform
        public int transformToInteger(String input) {
            return Integer.valueOf(input);
        }

    }

}
