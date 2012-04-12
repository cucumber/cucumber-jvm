package cucumber.runtime.java;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;

import cucumber.annotation.After;
import cucumber.annotation.AfterClass;
import cucumber.annotation.Before;
import cucumber.annotation.BeforeClass;
import cucumber.io.ClasspathResourceLoader;
import cucumber.runtime.Glue;

public class ClasspathMethodScannerTest {
    private ClasspathMethodScanner classpathMethodScanner;
    
    @org.junit.Before
    public void setUp(){
        ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(Thread.currentThread().getContextClassLoader());
        classpathMethodScanner = new ClasspathMethodScanner(resourceLoader);
    }
    
    
    @Test
    public void loadGlue_registers_the_methods_declaring_class_in_the_object_factory() throws NoSuchMethodException {
        ObjectFactory factory = Mockito.mock(ObjectFactory.class);
        Glue world = Mockito.mock(Glue.class);
        JavaBackend backend = new JavaBackend(factory);
        Whitebox.setInternalState(backend, "glue", world);

        // this delegates to classpathMethodScanner.scan which we test
        classpathMethodScanner.scan(backend, BaseStepDefs.class.getMethod("m"));

        verify(factory, times(1)).addClass(BaseStepDefs.class);
        verifyNoMoreInteractions(factory);
    }

    @Test
    public void scan_should_register_before_hooks() throws NoSuchMethodException, SecurityException {
        JavaBackend backend=Mockito.mock(JavaBackend.class);
        
        classpathMethodScanner.scan(backend, HookedStepdef.class.getMethod("before"));
        
        verify(backend).addHook(Matchers.isA(Before.class), Matchers.eq(HookedStepdef.class.getMethod("before")));
    }
    @Test
    public void scan_should_register_after_hooks() throws NoSuchMethodException, SecurityException {
        JavaBackend backend=Mockito.mock(JavaBackend.class);
        
        classpathMethodScanner.scan(backend, HookedStepdef.class.getMethod("after"));
        
        verify(backend).addHook(Matchers.isA(After.class), Matchers.eq(HookedStepdef.class.getMethod("after")));
    }
    @Test
    public void scan_should_register_before_class_hooks() throws NoSuchMethodException, SecurityException {
        JavaBackend backend=Mockito.mock(JavaBackend.class);
        
        classpathMethodScanner.scan(backend, HookedStepdef.class.getMethod("beforeClass"));
        
        verify(backend).addHook(Matchers.isA(BeforeClass.class), Matchers.eq(HookedStepdef.class.getMethod("beforeClass")));
    }
    @Test
    public void scan_should_register_after_class_hooks() throws NoSuchMethodException, SecurityException {
        JavaBackend backend=Mockito.mock(JavaBackend.class);
        
        classpathMethodScanner.scan(backend, HookedStepdef.class.getMethod("afterClass"));
        
        verify(backend).addHook(Matchers.isA(AfterClass.class), Matchers.eq(HookedStepdef.class.getMethod("afterClass")));
    }
    
    public static class Stepdefs2 extends BaseStepDefs {
        public interface Interface1 {
        }
    }
    
    public static class HookedStepdef{
        @Before
        public void before(){}
        @After
        public void after(){}
        @BeforeClass
        public static void beforeClass(){}
        @AfterClass
        public static void afterClass(){}
    }

    public static class BaseStepDefs {
        @Before
        public void m() {
        }
    }
}
