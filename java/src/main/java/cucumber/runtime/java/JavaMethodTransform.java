package cucumber.runtime.java;

import cucumber.runtime.transformers.Transformable;

import java.lang.reflect.Method;
import java.util.Locale;

public class JavaMethodTransform implements Transformable<Object> {

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
