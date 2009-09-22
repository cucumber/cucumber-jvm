package cuke4duke.internal.java;

import cuke4duke.internal.jvmclass.ClassLanguage;
import cuke4duke.internal.language.MethodInvoker;
import cuke4duke.internal.language.StepDefinition;
import cuke4duke.internal.language.Group;
import cuke4duke.internal.language.JdkRegexpGroup;
import org.jruby.RubyArray;

import java.lang.reflect.Method;
import java.util.regex.Pattern;
import java.util.List;

public class JavaStepDefinition implements StepDefinition {
    private final Pattern regexp;
    private final MethodInvoker methodInvoker;
    private final ClassLanguage classLanguage;
    private final Method method;

    public JavaStepDefinition(ClassLanguage classLanguage, Method method, Pattern regexp) {
        this.classLanguage = classLanguage;
        this.method = method; 
        methodInvoker = new MethodInvoker(method);
        this.regexp = regexp;
    }

    public List<Group> groups(String stepName) {
        return JdkRegexpGroup.groupsFrom(regexp, stepName);
    }

    public boolean op_equal(StepDefinition other) {
        throw new RuntimeException("GOT HERE");
    }

    public String file_colon_line() {
        return method.toGenericString();
    }

    public void invoke(RubyArray rubyArgs) throws Throwable {
        Object target = classLanguage.getTarget(method.getDeclaringClass());
        Class<?>[] types = method.getParameterTypes();
        methodInvoker.invoke(target, types, rubyArgs);
    }

}
