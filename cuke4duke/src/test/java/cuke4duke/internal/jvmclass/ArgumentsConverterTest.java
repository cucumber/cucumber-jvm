package cuke4duke.internal.jvmclass;

import cuke4duke.internal.language.AbstractProgrammingLanguage;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArgumentsConverterTest {
    private AbstractProgrammingLanguage p;

    @Before
    public void setup() {
        p = new AbstractProgrammingLanguage(null) {
            @Override
            public void load_code_file(String file) throws Throwable {
            }

            @Override
            protected void begin_scenario(IRubyObject scenario) throws Throwable {
            }

            @Override
            public void end_scenario() throws Throwable {
            }

            @Override
            protected Object customTransform(Object arg, Class<?> parameterType) throws Throwable {
                return null;
            }
        };
    }

    @Test
    public void shouldConvertFromStringToObject() throws Throwable {
        assertEquals("An Object", p.transformOne("An Object", Object.class));
    }
    
    @Test
    public void shouldConvertFromStringToInt() throws Throwable {
        assertEquals(3, p.transformOne("3", Integer.TYPE));
    }

    @Test
    public void shouldConvertFromStringToInteger() throws Throwable {
        assertEquals(4, p.transformOne("4", Integer.class));
    }

    @Test
    public void shouldConvertFromStringToLongPrimitive() throws Throwable {
        assertEquals(3L, p.transformOne("3", Long.TYPE));
    }

    @Test
    public void shouldConvertFromStringToLong() throws Throwable {
        assertEquals(4L, p.transformOne("4", Long.class));
    }
}
