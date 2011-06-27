package cucumber.runtime.clojure;

import clojure.lang.AFunction;

import java.util.List;

public class CljHook {
    private final AFunction closure;

    public CljHook(List<String> tagExpressions, AFunction closure) {
        //super(tagExpressions);
        this.closure = closure;
    }

    public void invoke(String location, Object scenario) throws Throwable {
        closure.call();
    }
}
