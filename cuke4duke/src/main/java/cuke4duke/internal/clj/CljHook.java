package cuke4duke.internal.clj;

import clojure.lang.AFunction;
import cuke4duke.Scenario;
import cuke4duke.internal.language.AbstractHook;

import java.util.List;

public class CljHook extends AbstractHook {
    private final AFunction closure;

    public CljHook(List<String> tagExpressions, AFunction closure) {
        super(tagExpressions);
        this.closure = closure;
    }

    public void invoke(String location, Scenario scenario) throws Throwable {
        closure.call();
    }
}
