package cucumber.runtime.clojure;

import clojure.lang.IFn;

import java.util.List;

public class ClojureHook {
    private final IFn closure;

    public ClojureHook(List<String> tagExpressions, IFn closure) {
        //super(tagExpressions);
        this.closure = closure;
    }

    public void invoke(String location, Object scenario) throws Throwable {
        closure.call();
    }
}
