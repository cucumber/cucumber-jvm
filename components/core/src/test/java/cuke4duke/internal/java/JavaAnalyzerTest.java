package cuke4duke.internal.java;

import cucumber.annotation.I18n.EN.*;
import cuke4duke.StepMother;
import cuke4duke.internal.jvmclass.ClassAnalyzer;
import cuke4duke.internal.jvmclass.ClassLanguage;
import cuke4duke.internal.jvmclass.ClassLanguageMixin;
import cuke4duke.internal.language.AbstractStepDefinition;
import cuke4duke.internal.language.StepDefinition;
import cuke4duke.spi.ExceptionFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

public class JavaAnalyzerTest {

    @Mock
    private StepMother stepMother;
    @Mock
    private ClassLanguageMixin languageMixin;

    public JavaAnalyzerTest() {
        initMocks(this);
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
    public void dummy() throws Throwable {
    }

    @Test
    @Ignore
    public void shouldAllowOneInheritedSubclass() throws Throwable {
        ClassLanguage classLanguage = new ClassLanguage(languageMixin, mock(ExceptionFactory.class), stepMother, Arrays.<ClassAnalyzer>asList(new JavaAnalyzer()));
        classLanguage.addClass(FlintStone.class);
        classLanguage.addClass(Fred.class);
        classLanguage.begin_scenario(null);
        List<StepDefinition> stepDefinitions = classLanguage.getStepDefinitions();
        assertEquals(1, stepDefinitions.size());

        assertEquals(Fred.class, ((AbstractStepDefinition)stepDefinitions.get(0)).invokeWithArgs(new Object[0]));
    }

    @Test(expected=Exception.class)
    public void shouldFailWithTwoInheritedSubclass() throws Throwable {
        ClassLanguage classLanguage = new ClassLanguage(languageMixin, mock(ExceptionFactory.class), stepMother, Arrays.<ClassAnalyzer>asList(new JavaAnalyzer()));
        classLanguage.addClass(FlintStone.class);
        classLanguage.addClass(Fred.class);
        classLanguage.addClass(Wilma.class);
        classLanguage.begin_scenario(null);
        List<StepDefinition> stepDefinitions = classLanguage.getStepDefinitions();
        assertEquals(1, stepDefinitions.size());

        assertEquals(Fred.class, ((AbstractStepDefinition)stepDefinitions.get(0)).invokeWithArgs(new Object[0]));
    }
}
