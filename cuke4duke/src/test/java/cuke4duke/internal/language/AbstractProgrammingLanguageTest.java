package cuke4duke.internal.language;

import cuke4duke.internal.java.JavaTransform;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;


public class AbstractProgrammingLanguageTest {

    private JavaTransform transform;

    @Before
    public void setUp() {
        this.transform = new JavaTransform(null, null);
    }

    @Test
    public void shouldAddTransformHooksToTransformsMap() throws Throwable {
        AbstractProgrammingLanguage programmingLanguage = new TestProgrammingLanguage(null);
        programmingLanguage.begin_scenario(null);
        programmingLanguage.addTransform(Integer.TYPE, transform);
        assertEquals(transform, programmingLanguage.getTransforms().get(Integer.TYPE));
    }

    private class TestProgrammingLanguage extends AbstractProgrammingLanguage {

        public TestProgrammingLanguage(LanguageMixin languageMixin) {
            super(languageMixin);
        }

        @Override
        public void end_scenario() throws Throwable {
        }

        @Override
        public void load_code_file(String file) throws Throwable {
        }

        @Override
        protected void begin_scenario(IRubyObject scenario) throws Throwable {
            clearHooksAndStepDefinitions();
        }
    }
}
