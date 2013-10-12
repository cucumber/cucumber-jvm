package cucumber.runtime.ioke;

import cucumber.api.DataTable;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Glue;
import cucumber.runtime.UnreportedStepExecutor;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.snippets.FunctionNameGenerator;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Step;
import ioke.lang.IokeObject;
import ioke.lang.Message;
import ioke.lang.Runtime;
import ioke.lang.exceptions.ControlFlow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IokeBackend implements Backend {
    private final SnippetGenerator snippetGenerator = new SnippetGenerator(new IokeSnippet());
    private final ResourceLoader resourceLoader;
    private final Runtime ioke;
    private final List<Runtime.RescueInfo> failureRescues;
    private final List<Runtime.RescueInfo> pendingRescues;
    private String currentLocation;
    private Glue glue;

    public IokeBackend(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        try {
            ioke = new Runtime();
            ioke.init();
            ioke.ground.setCell("IokeBackend", this);
            ioke.evaluateString("use(\"cucumber/runtime/ioke/dsl\")");
            failureRescues = createRescues("ISpec", "ExpectationNotMet");
            pendingRescues = createRescues("Pending");
        } catch (Throwable e) {
            throw new CucumberException("Failed to initialize Ioke", e);
        }
    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
        this.glue = glue;

        for (String gluePath : gluePaths) {
            for (Resource resource : resourceLoader.resources(gluePath, ".ik")) {
                currentLocation = resource.getPath();
                evaluate(resource);
            }
        }
    }

    @Override
    public void setUnreportedStepExecutor(UnreportedStepExecutor executor) {
        //Not used yet
    }

    @Override
    public void buildWorld() {
    }

    private void evaluate(Resource resource) {
        try {
            String path = resource.getPath().replace('\\', '/'); // Need forward paths, even when on Windows
            ioke.evaluateString("use(\"" + path + "\")");
        } catch (ControlFlow controlFlow) {
            throw new CucumberException(controlFlow);
        }
    }

    @Override
    public void disposeWorld() {
    }

    @Override
    public String getSnippet(Step step, FunctionNameGenerator functionNameGenerator) {
        return snippetGenerator.getSnippet(step, functionNameGenerator);
    }

    public void addStepDefinition(Object iokeStepDefObject) throws Throwable {
        glue.addStepDefinition(new IokeStepDefinition(this, ioke, (IokeObject) iokeStepDefObject, currentLocation));
    }

    private List<Runtime.RescueInfo> createRescues(String... names) throws ControlFlow {
        IokeObject condition = IokeObject.as(IokeObject.getCellChain(ioke.condition,
                ioke.message,
                ioke.ground,
                names), ioke.ground);
        List<Runtime.RescueInfo> rescues = new ArrayList<Runtime.RescueInfo>();
        IokeObject rr = IokeObject.as(((Message) IokeObject.data(ioke.mimic)).sendTo(ioke.mimic, ioke.ground, ioke.rescue), ioke.ground);
        List<Object> conds = new ArrayList<Object>();
        conds.add(condition);
        rescues.add(new Runtime.RescueInfo(rr, conds, rescues, ioke.getBindIndex()));
        return rescues;
    }

    void execute(IokeObject iokeStepDefObject, Object[] args) throws Throwable {
        try {
            ioke.registerRescues(failureRescues);
            ioke.registerRescues(pendingRescues);
            invoke(iokeStepDefObject, "invoke", multilineArg(args));
        } catch (ControlFlow.Rescue e) {
            // We may handle these differently in the future...
            if (e.getRescue().token == pendingRescues) {
                throw e;
            } else if (e.getRescue().token == failureRescues) {
                Message message = (Message) IokeObject.data(ioke.reportMessage);
                String errorMessage = message.sendTo(ioke.reportMessage, ioke.ground, e.getCondition()).toString();
                throw new AssertionError(errorMessage);
            } else {
                throw e;
            }
        } finally {
            ioke.unregisterRescues(failureRescues);
            ioke.unregisterRescues(pendingRescues);
        }

    }

    private Object multilineArg(Object[] args) {
        Object multilineArg;
        if (args.length > 0) {
            if (args[args.length - 1] instanceof String) {
                multilineArg = ioke.newText((String) args[args.length - 1]);
            } else if (args[args.length - 1] instanceof DataTable) {
                multilineArg = args[args.length - 1];
            } else {
                multilineArg = ioke.nil;
            }
        } else {
            multilineArg = ioke.nil;
        }
        return multilineArg;
    }

    Object invoke(IokeObject iokeStepDefObject, String message, Object... args) throws ControlFlow {
        IokeObject msg = ioke.newMessage(message);
        Message m = (Message) IokeObject.data(msg);
        return m.sendTo(msg, iokeStepDefObject, iokeStepDefObject, Arrays.asList(args));
    }
}
