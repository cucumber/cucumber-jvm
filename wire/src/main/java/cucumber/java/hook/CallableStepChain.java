package cucumber.java.hook;

public class CallableStepChain implements CallableStep {
    private StepCallChain scc;

    public CallableStepChain(StepCallChain scc) {
        this.scc = scc;
    }

    public void call() throws Throwable {
        scc.execNext();
    }
}
