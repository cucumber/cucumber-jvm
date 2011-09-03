package cucumber.runtime.java;

import cucumber.annotation.en.Given;
import cucumber.runtime.StepDefinition;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class JavaStepDefinitionDependencyInjectionTest {

    private static final Method GIVEN;
    private static final Method OTHER_GIVEN;

    static {
        try {
            GIVEN = Steps.class.getMethod("givenStep");
            OTHER_GIVEN = OtherSteps.class.getMethod("givenStep");
        } catch (NoSuchMethodException e) {
            throw new InternalError("dang");
        }
    }

    List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();
    private ObjectFactory mockObjectFactory = mock(ObjectFactory.class);
    private JavaBackend backend = new JavaBackend(mockObjectFactory, stepDefinitions);

    @Test
    public void constructor_arguments_get_registered() {
        backend.addStepDefinition(Pattern.compile("not relevant"), GIVEN);
        verify(mockObjectFactory).addClass(Steps.class);
        verify(mockObjectFactory).addClass(StepContext1.class);
        verify(mockObjectFactory).addClass(StepContext2.class);
    }

    @Test @Ignore("Currently there is no way to check if a class was already registered with the objectfactory!")
    public void constructor_arguments_get_registered_exactly_once() {
        backend.addStepDefinition(Pattern.compile("not relevant"), OTHER_GIVEN);
        verify(mockObjectFactory, times(1)).addClass(OtherSteps.class);
        verify(mockObjectFactory, times(1)).addClass(StepContext3.class);
        verify(mockObjectFactory, times(1)).addClass(StepContext4.class);
    }

    public class Steps {

        public Steps(StepContext1 context1, StepContext2 context2) {
        }

        @Given("whatever")
        public void givenStep() {
        }
    }

    public class StepContext1 {
    }

    public class StepContext2 {
    }


    public class OtherSteps {

        public OtherSteps(StepContext3 context3, StepContext4 context4) {
        }

        @Given("whatever")
        public void givenStep() {
        }
    }

    public class StepContext3 {
        public StepContext3(StepContext4 context4) {
        }
    }

    public class StepContext4 {
    }
}
