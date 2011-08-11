package cucumber.runtime.java;

import cucumber.runtime.transformers.Transformer;

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

    public Object transform(String argument, Locale locale) {
        return this.backend.invoke(this.transformMethod, new Object[]{argument});
    }

}
