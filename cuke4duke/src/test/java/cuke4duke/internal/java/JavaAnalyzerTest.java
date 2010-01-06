package cuke4duke.internal.java;

import cuke4duke.Given;
import cuke4duke.StepMother;
import cuke4duke.Transform;
import cuke4duke.internal.jvmclass.*;
import cuke4duke.internal.language.AbstractStepDefinition;
import cuke4duke.internal.language.StepDefinition;
import cuke4duke.internal.language.Transformable;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    @Mock
    private StepMother stepMother;
    @Mock
    private ClassLanguageMixin languageMixin;

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

        ArgumentCaptor<Transformable> transformableArgument = ArgumentCaptor.forClass(Transformable.class);
        ArgumentCaptor<Class> returnTypeArgument = ArgumentCaptor.forClass(Class.class);
//        verify(classLanguage).addTransform(returnTypeArgument.capture(), transformableArgument.capture());
        
        Class returnType = returnTypeArgument.getValue();
        Transformable transform = transformableArgument.getValue();
        
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

    public abstract static class FlintStone {
        @Given("where is dino")
        public Class whereIsDino() {
            return getClass();
        }
    }

    public static class Fred extends FlintStone {
    }

    public static class Wilma extends FlintStone {
    }

    @Test
    public void shouldAllowOneInheritedSubclass() throws Throwable {
        ClassLanguage classLanguage = new ClassLanguage(languageMixin, stepMother, Arrays.<ClassAnalyzer>asList(javaAnalyzer));
        classLanguage.addClass(FlintStone.class);
        classLanguage.addClass(Fred.class);
        classLanguage.begin_scenario(null);
        List<StepDefinition> stepDefinitions = classLanguage.getStepDefinitions();
        assertEquals(1, stepDefinitions.size());

        assertEquals(Fred.class, ((AbstractStepDefinition)stepDefinitions.get(0)).invokeWithArgs(new Object[0]));
    }

    @Test(expected=Exception.class)
    public void shouldFailWithTwoInheritedSubclass() throws Throwable {
        ClassLanguage classLanguage = new ClassLanguage(languageMixin, stepMother, Arrays.<ClassAnalyzer>asList(javaAnalyzer));
        classLanguage.addClass(FlintStone.class);
        classLanguage.addClass(Fred.class);
        classLanguage.addClass(Wilma.class);
        classLanguage.begin_scenario(null);
        List<StepDefinition> stepDefinitions = classLanguage.getStepDefinitions();
        assertEquals(1, stepDefinitions.size());

        assertEquals(Fred.class, ((AbstractStepDefinition)stepDefinitions.get(0)).invokeWithArgs(new Object[0]));
    }
}
