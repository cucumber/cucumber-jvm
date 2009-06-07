package cuke4duke.internal;

import java.lang.reflect.Method;

public class Hook extends Invokable {
    private final String tags;

    public Hook(Object object, Method method, String tags) {
        super(object, method);
        this.tags = tags;
    }
}
