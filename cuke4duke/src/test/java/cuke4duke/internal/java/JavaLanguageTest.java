package cuke4duke.internal.java;

import org.junit.Test;
import static org.mockito.Mockito.mock;
import cuke4duke.internal.language.ProgrammingLanguage;
import cuke4duke.internal.language.LanguageMixin;

public class JavaLanguageTest {
    @Test
    public void shouldLoadExistingClassFromJavaFileName() throws Throwable {
        ProgrammingLanguage language = new JavaLanguage(mock(LanguageMixin.class));
        language.step_definitions_for("foo/java/lang/String.java");
    }

    @Test(expected = ClassNotFoundException.class)
    public void shouldFailToLoadMissingClassFromJavaFileName() throws Throwable {
        ProgrammingLanguage language = new JavaLanguage(mock(LanguageMixin.class));
        language.step_definitions_for("foo/java/lang/Strix.java");
    }
}
