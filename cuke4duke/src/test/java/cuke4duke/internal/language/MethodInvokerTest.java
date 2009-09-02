package cuke4duke.internal.language;

import org.junit.Test;
import org.junit.Before;
import org.jruby.RubyArray;

import java.lang.reflect.Method;
import cuke4duke.Pending;
import cuke4duke.internal.JRuby;

public class MethodInvokerTest {
    private class SomethingWithPending {
        @Pending
        public void dontExecuteMe() {
            throw new RuntimeException("Shouldn't be executed");
        }
    }

    @Before
    public void definePendingException() {
        JRuby.getRuntime().evalScriptlet("module Cucumber;class Pending < StandardError;end;end");
    }

    @Test(expected = org.jruby.exceptions.RaiseException.class)
    public void shouldRaiseCucumberPendingWhenAnnotatedWithPending() throws Throwable {
        Method dontExecuteMe = SomethingWithPending.class.getDeclaredMethod("dontExecuteMe");
        MethodInvoker mi = new MethodInvoker(dontExecuteMe);
        RubyArray emptyArgs = RubyArray.newArray(JRuby.getRuntime());
        mi.invoke(new SomethingWithPending(), new Class[]{}, emptyArgs);
    }
}
