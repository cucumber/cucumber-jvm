package cuke4duke.internal.jvmclass;

import org.junit.Test;
import org.junit.Before;
import static org.mockito.Mockito.mock;
import cuke4duke.internal.language.ProgrammingLanguage;
import cuke4duke.internal.language.LanguageMixin;

import java.util.Collections;

public class ClassLanguageTest {
    private ProgrammingLanguage language;

    @Before
    public void createLanguage() throws Throwable {
        language = new ClassLanguage(mock(ClassLanguageMixin.class), Collections.<ClassAnalyzer>emptyList());
    }

    @Test
    public void shouldLoadExistingClassFromJavaFileName() throws Throwable {
        language.step_definitions_for("foo/java/lang/String.class");
    }

    @Test(expected = ClassNotFoundException.class)
    public void shouldFailToLoadMissingClassFromJavaFileName() throws Throwable {
        language.step_definitions_for("foo/java/lang/Strix.class");
    }
}
