package cuke4duke.internal.java;

import cuke4duke.annotation.Pending;
import cuke4duke.internal.JRuby;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

public class MethodInvokerTest {
    private class SomethingWithPending {
		@Pending
		@SuppressWarnings("unused")
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
        MethodInvoker mi = new MethodInvoker();
        mi.invoke(dontExecuteMe, new SomethingWithPending(), new Object[0]);
    }
}
