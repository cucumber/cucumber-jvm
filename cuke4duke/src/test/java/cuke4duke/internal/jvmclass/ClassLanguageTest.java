package cuke4duke.internal.jvmclass;

import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import cuke4duke.StepMother;

public class ClassLanguageTest {
    private ClassLanguage language;

    @Before
    public void createLanguage() throws Throwable {
        language = new ClassLanguage(mock(ClassLanguageMixin.class), mock(StepMother.class), Collections.<ClassAnalyzer> emptyList());
    }

    @Test
    public void shouldLoadExistingClassFromJavaFileName() throws Throwable {
        language.load_code_file("foo/java/lang/String.class");
    }

    @Test(expected = ClassNotFoundException.class)
    public void shouldFailToLoadMissingClassFromJavaFileName() throws Throwable {
        language.load_code_file("foo/java/lang/Strix.class");
    }

}
