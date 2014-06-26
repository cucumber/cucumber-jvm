package cucumber.runtime.java;

import cucumber.api.java.*;

import java.lang.annotation.Annotation;

enum HookType {
    BEFORE_ALL(BeforeAll.class),
    AFTER_ALL(AfterAll.class),

    BEFORE(Before.class),
    AFTER(After.class);

    private Class<? extends Annotation> type;

    private HookType(Class<? extends Annotation> type) {
        this.type = type;
    }

    private Class<? extends Annotation> getType() {
        return type;
    }

    public static HookType fromAnnotation(Annotation annotation) {
        for (HookType hookType : values())
            if (hookType.type.equals(annotation.annotationType())) return hookType;

        return null;
    }
}