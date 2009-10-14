package cuke4duke.internal.jvmclass;

import cuke4duke.internal.language.ProgrammingLanguage;
import cuke4duke.StepMother;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;

import java.util.Collections;

public class ClassLanguageTest {
    private ProgrammingLanguage language;

    @Before
    public void createLanguage() throws Throwable {
        language = new ClassLanguage(mock(ClassLanguageMixin.class), mock(StepMother.class), Collections.<ClassAnalyzer>emptyList());
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
