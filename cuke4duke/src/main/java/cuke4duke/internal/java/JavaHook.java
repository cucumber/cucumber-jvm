package cuke4duke.internal.java;

import cuke4duke.internal.JRuby;
import cuke4duke.internal.jvmclass.ClassLanguage;
import cuke4duke.internal.jvmclass.ObjectFactory;
import cuke4duke.internal.language.AbstractHook;
import org.jruby.runtime.builtin.IRubyObject;
import scala.tools.nsc.Global;

import java.lang.reflect.Method;

public class JavaHook extends AbstractHook {
    private final ClassLanguage classLanguage;
    private final Method method;

    public JavaHook(ClassLanguage classLanguage, Method method, String[] tagExpressions) {
        super(tagExpressions);
        this.classLanguage = classLanguage;

        this.method = method;
    }
    
    public void invoke(String location, IRubyObject scenario) throws Throwable {
        classLanguage.invokeHook(method, scenario);
    }
}
