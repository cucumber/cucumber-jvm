package cuke4duke.internal.java;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import cuke4duke.Transform;
import cuke4duke.internal.jvmclass.ClassLanguage;
import cuke4duke.internal.jvmclass.ObjectFactory;

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
        javaAnalyzer.populateStepDefinitionsAndHooksFor(transformer.getClass(), objectFactory, classLanguage);
        ArgumentCaptor<JavaHook> hookArgument = ArgumentCaptor.forClass(JavaHook.class);
        ArgumentCaptor<Class> returnTypeArgument = ArgumentCaptor.forClass(Class.class);
        verify(classLanguage).addTransformHook(returnTypeArgument.capture(), hookArgument.capture());
        
        Class returnType = returnTypeArgument.getValue();
        JavaHook hook = hookArgument.getValue();
        
        assertTrue(returnType.isAssignableFrom(Integer.TYPE));

        for (Field field : hook.getClass().getDeclaredFields()) {
            if (field.getDeclaringClass().isAssignableFrom(Method.class)) {
                field.setAccessible(true);
                assertEquals(((Method) field.get(hook)).getName(), transformer.getClass().getDeclaredMethods()[0]);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void shouldAddDefaultJavaTransformsToClassLanguage() throws Throwable {
        javaAnalyzer.populateStepDefinitionsAndHooksFor(transformer.getClass(), objectFactory, classLanguage);
        ArgumentCaptor<JavaHook> hookArgument = ArgumentCaptor.forClass(JavaHook.class);
        ArgumentCaptor<Class> returnTypeArgument = ArgumentCaptor.forClass(Class.class);
        verify(classLanguage).addTransformHook(returnTypeArgument.capture(), hookArgument.capture());
        
        Class returnType = returnTypeArgument.getValue();
        JavaHook hook = hookArgument.getValue();
        
        assertTrue(returnType.isAssignableFrom(Integer.TYPE));

        for (Field field : hook.getClass().getDeclaredFields()) {
            if (field.getDeclaringClass().isAssignableFrom(Method.class)) {
                field.setAccessible(true);
                assertEquals(((Method) field.get(hook)).getName(), transformer.getClass().getDeclaredMethods()[0]);
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
