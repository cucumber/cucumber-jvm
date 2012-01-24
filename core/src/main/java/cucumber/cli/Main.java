package cucumber.cli;

public class Main {

    public static void main(String[] $argv) throws Throwable {
        // TODO: need to wrap this in some tests and mock out System.exit and System.out.
        RuntimeOptions options = new RuntimeOptions();

        options.parse($argv);

        PrintMessage printMessage = new PrintMessage(System.err);

        options.applyIfVersionRequestedTo(printMessage);
        options.applyErrorsTo(printMessage);
        options.applyIfHelpRequestedTo(printMessage);

        if(printMessage.lineCount() > 0) System.exit(1);

        RuntimeActions runtimeActions = new RuntimeActions();
        System.exit(runtimeActions.apply(options));
    }
}
