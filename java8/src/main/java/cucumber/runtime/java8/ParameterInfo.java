package cucumber.runtime.java8;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

class ParameterInfo {
    private final Type type;

    static List<ParameterInfo> fromTypes(Type[] genericParameterTypes) {
        List<ParameterInfo> result = new ArrayList<>();
        for (Type genericParameterType : genericParameterTypes) {
            for (Annotation annotation : genericParameterType.getClass().getAnnotations()) {
                System.out.println(annotation.toString());
            }


            result.add(new ParameterInfo(genericParameterType));
        }
        return result;
    }

    private ParameterInfo(Type type) {
        this.type = type;
    }

    Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.toString();
    }

}
