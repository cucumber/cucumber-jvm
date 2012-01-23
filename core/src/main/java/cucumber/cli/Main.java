package cucumber.cli;

public class Main {

    public static void main(String[] $argv) throws Throwable {
        // TODO: need to wrap this in some tests and mock out System.exit and System.out. Should probably look at slf4j.
        RuntimeOptions options = new RuntimeOptions();

        options.parse($argv);

        PrintMessage printMessage = new PrintMessage(System.err);
        options.applyVersionRequested(printMessage);
        options.applyErrors(printMessage);
        options.applyHelpRequested(printMessage);
        if(printMessage.lineCount() > 0) System.exit(1);

        RuntimeActions runtimeActions = new RuntimeActions();
        System.exit(runtimeActions.apply(options));
    }
}
