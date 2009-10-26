package cuke4duke.internal.language;

import static junit.framework.Assert.assertEquals;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import cuke4duke.internal.java.JavaTransform;


public class AbstractProgrammingLanguageTest {

    private JavaTransform transform;

    @Before
    public void setUp() {
        this.transform = new JavaTransform(null, null);
    }

    @Test
    public void shouldAddTransformHooksToTransformsMap() throws Throwable {
        AbstractProgrammingLanguage programmingLanguage = new TestProgrammingLanguage(null);
        programmingLanguage.prepareScenario();
        programmingLanguage.addTransform(Integer.TYPE, transform);
        assertEquals(transform, programmingLanguage.getTransforms().get(Integer.TYPE));
    }

    private class TestProgrammingLanguage extends AbstractProgrammingLanguage {

        public TestProgrammingLanguage(LanguageMixin languageMixin) {
            super(languageMixin);
        }

        @Override
        public void cleanupScenario() throws Throwable {
        }

        @Override
        public void load_code_file(String file) throws Throwable {
        }

        @Override
        protected void prepareScenario() throws Throwable {
            clearHooksAndStepDefinitions();
        }
    }
}
