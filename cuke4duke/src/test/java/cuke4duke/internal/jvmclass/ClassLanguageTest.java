package cuke4duke.internal.jvmclass;

import org.junit.Test;
import static org.mockito.Mockito.mock;
import cuke4duke.internal.language.ProgrammingLanguage;
import cuke4duke.internal.language.LanguageMixin;

public class ClassLanguageTest {
    @Test
    public void shouldLoadExistingClassFromJavaFileName() throws Throwable {
        ProgrammingLanguage language = new ClassLanguage(mock(LanguageMixin.class));
        language.step_definitions_for("foo/java/lang/String.class");
    }

    @Test(expected = ClassNotFoundException.class)
    public void shouldFailToLoadMissingClassFromJavaFileName() throws Throwable {
        ProgrammingLanguage language = new ClassLanguage(mock(LanguageMixin.class));
        language.step_definitions_for("foo/java/lang/Strix.class");
    }
}
