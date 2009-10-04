package cuke4duke.internal.java;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import cuke4duke.app.HelloService;
import cuke4duke.app.PicoContainerHelloService;
import cuke4duke.internal.jvmclass.ClassLanguage;
import cuke4duke.internal.jvmclass.ObjectFactory;
import cuke4duke.internal.language.Hook;
import cuke4duke.steps.PicoContainerSteps;

public class JavaAnalyzerTest {
    
    private JavaAnalyzer javaAnalyzer;
    private TestTransformer transformer;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private ClassLanguage classLanguage;
    
    public JavaAnalyzerTest() {
        initMocks(this);
        this.javaAnalyzer = new JavaAnalyzer();
        this.transformer = new TestTransformer();
    }
    
    @Test
    public void shouldAddTransformToClassLanguage() {
        List<String> tagNames = Arrays.asList("");
        Hook transformer = new JavaHook(tagNames , method, objectFactory)
        javaAnalyzer.populateStepDefinitionsAndHooksFor(transformer.getClass(), objectFactory, classLanguage);
        verify(classLanguage).addTransformer(Mockito.e);
    }
    
    private class TestTransformer {
        
    }

}
