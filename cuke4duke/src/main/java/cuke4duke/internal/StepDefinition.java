package cuke4duke.internal;

import java.lang.reflect.Method;

public class StepDefinition extends Invokable {
    private final String regexpString;

    public StepDefinition(Object target, Method method, String regexpString) {
        super(target, method);
        this.regexpString = regexpString;
    }

    public String getRegexpString() {
        return regexpString;
    }

    public String file_colon_line() {
        return method.toGenericString();
    }
}
