package cuke4duke.internal.jvmclass;

import cuke4duke.Scenario;
import cuke4duke.internal.language.AbstractProgrammingLanguage;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class ArgumentsConverterTest {
    private AbstractProgrammingLanguage p;

    @Before
    public void setup() {
        p = new AbstractProgrammingLanguage(null, null) {
            @Override
            public void load_code_file(String file) throws Throwable {
            }

            @Override
            protected void begin_scenario(Scenario scenario) throws Throwable {
            }

            @Override
            public void end_scenario() throws Throwable {
            }

            @Override
            protected Object customTransform(Object arg, Class<?> parameterType, Locale locale) throws Throwable {
                return null;
            }
        };
    }

    @Test
    public void shouldConvertFromStringToObject() throws Throwable {
        assertEquals("An Object", p.transformOne("An Object", Object.class, Locale.getDefault()));
    }
    
    @Test
    public void shouldConvertFromStringToInt() throws Throwable {
        assertEquals(3, p.transformOne("3", Integer.TYPE, Locale.getDefault()));
    }

    @Test
    public void shouldConvertFromStringToInteger() throws Throwable {
        assertEquals(4, p.transformOne("4", Integer.class, Locale.getDefault()));
    }

    @Test
    public void shouldConvertFromStringToLongPrimitive() throws Throwable {
        assertEquals(3L, p.transformOne("3", Long.TYPE, Locale.getDefault()));
    }

    @Test
    public void shouldConvertFromStringToLong() throws Throwable {
        assertEquals(4L, p.transformOne("4", Long.class, Locale.getDefault()));
    }
}
