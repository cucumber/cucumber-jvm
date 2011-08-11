package cucumber.runtime.java;

import cucumber.runtime.transformers.Transformer;
import cucumber.runtime.transformers.Transformers;

import java.lang.reflect.Method;
import java.util.Locale;

public class JavaMethodTransform implements Transformer<Object> {

    private Method transformMethod;
    private JavaBackend backend;

    public JavaMethodTransform(Method transformMethod, JavaBackend backend) {
        super();
        this.transformMethod = transformMethod;
        this.backend = backend;
    }

    public Object transform(Locale locale, String... arguments) {
        Transformers transformers = new Transformers();
        Object[] transformedArguments = new Object[arguments.length];
        for(int i = 0; i < arguments.length; i++) {
            transformedArguments[i] = transformers.transform(locale, transformMethod.getParameterTypes()[i], arguments[i]);
        }
        return this.backend.invoke(this.transformMethod, transformedArguments);
    }

}
