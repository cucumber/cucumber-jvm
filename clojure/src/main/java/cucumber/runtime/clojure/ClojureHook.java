package cucumber.runtime.clojure;

import clojure.lang.AFunction;

import java.util.List;

public class ClojureHook {
    private final AFunction closure;

    public ClojureHook(List<String> tagExpressions, AFunction closure) {
        //super(tagExpressions);
        this.closure = closure;
    }

    public void invoke(String location, Object scenario) throws Throwable {
        closure.call();
    }
}
