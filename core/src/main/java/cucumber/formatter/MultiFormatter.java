package cucumber.formatter;

import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class MultiFormatter {
    private final List<Formatter> formatters = new ArrayList<Formatter>();
    private final ClassLoader classLoader;

    public MultiFormatter(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void add(Formatter formatter) {
        formatters.add(formatter);
    }

    public boolean isEmpty() {
        return formatters.isEmpty();
    }

    public Formatter formatterProxy() {
        return (Formatter) Proxy.newProxyInstance(classLoader, new Class<?>[]{Formatter.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object target, Method method, Object[] args) throws Throwable {
                for (Formatter formatter : formatters) {
                    method.invoke(formatter, args);
                }
                return null;
            }
        });
    }

    public Reporter reporterProxy() {
        return (Reporter) Proxy.newProxyInstance(classLoader, new Class<?>[]{Reporter.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object target, Method method, Object[] args) throws Throwable {
                for (Formatter formatter : formatters) {
                    if (formatter instanceof Reporter) {
                        method.invoke(formatter, args);
                    }
                }
                return null;
            }
        });
    }
}
