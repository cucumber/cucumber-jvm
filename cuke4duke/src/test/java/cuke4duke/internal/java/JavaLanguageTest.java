package cuke4duke.internal.java;

import org.junit.Test;
import cuke4duke.internal.language.ProgrammingLanguage;

public class JavaLanguageTest {
    @Test
    public void shouldLoadExistingClassFromJavaFileName() throws Throwable {
        ProgrammingLanguage language = new JavaLanguage(null);
        language.load_step_def_file("foo/java/lang/String.java");
    }

    @Test(expected = ClassNotFoundException.class)
    public void shouldFailToLoadMissingClassFromJavaFileName() throws Throwable {
        ProgrammingLanguage language = new JavaLanguage(null);
        language.load_step_def_file("foo/java/lang/Strix.java");
    }
}
