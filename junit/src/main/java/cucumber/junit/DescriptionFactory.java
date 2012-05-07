package cucumber.junit;

import cucumber.runtime.CucumberException;
import org.junit.runner.Description;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class attempts to create descriptions with unique ids, if the method is available.
 * Falls back to not using uniqueId if not.
 * <p/>
 * See <a href="https://github.com/cucumber/cucumber-jvm/issues/225">#225</a> for details.
 */
class DescriptionFactory {
    private static Method CREATE_SUITE_DESCRIPTION;
    private static boolean USE_UNIQUE_ID = false;
    private static String UNIQUE_HACK = "";

    static {
        try {
            CREATE_SUITE_DESCRIPTION = Description.class.getMethod("createSuiteDescription", String.class, Object.class, Array.newInstance(Annotation.class, 0).getClass());
            USE_UNIQUE_ID = true;
        } catch (NoSuchMethodException e) {
            try {
                CREATE_SUITE_DESCRIPTION = Description.class.getMethod("createSuiteDescription", String.class, Array.newInstance(Annotation.class, 0).getClass());
                USE_UNIQUE_ID = false;
            } catch (NoSuchMethodException e1) {
                throw new CucumberException("You need JUnit 4.10 or newer");
            }
        }
    }

    public static Description createDescription(String name, Object uniqueId) {
        try {
            if (USE_UNIQUE_ID) {
                return (Description) CREATE_SUITE_DESCRIPTION.invoke(null, name, uniqueId, Array.newInstance(Annotation.class, 0));
            } else {
                UNIQUE_HACK += " ";
                return (Description) CREATE_SUITE_DESCRIPTION.invoke(null, name + UNIQUE_HACK, Array.newInstance(Annotation.class, 0));
            }
        } catch (IllegalAccessException e) {
            throw new CucumberException("Failed to create a description", e);
        } catch (InvocationTargetException e) {
            throw new CucumberException("Failed to create a description", e.getTargetException());
        }
    }
}
