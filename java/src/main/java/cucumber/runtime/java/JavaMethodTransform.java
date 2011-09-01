package cucumber.runtime.java;

import cucumber.runtime.transformers.TransformationException;
import cucumber.runtime.transformers.Transformer;
import cucumber.runtime.transformers.Transformers;

import java.lang.reflect.Method;
import java.util.Locale;

// TODO: Where is this used other than in a test??
@Deprecated
public class JavaMethodTransform implements Transformer<Object> {

    private final Method transformMethod;
    private final JavaBackend backend;

    public JavaMethodTransform(Method transformMethod, JavaBackend backend) {
        this.transformMethod = transformMethod;
        this.backend = backend;
    }

    public Object transform(Locale locale, String... arguments) throws TransformationException {
        Transformers transformers = new Transformers();
        Object[] transformedArguments = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            transformedArguments[i] = transformers.transform(locale, transformMethod.getParameterTypes()[i], arguments[i]);
        }
        return backend.invoke(transformMethod, transformedArguments);
    }

}
