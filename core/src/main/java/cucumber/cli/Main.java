package cucumber.cli;

public class Main {

    public static void main(String[] $argv) throws Throwable {
        // TODO: need to wrap this in some tests and mock out System.exit and System.out. Should probably look at slf4j.
        RuntimeOptions options = new RuntimeOptions();

        options.parse($argv);

        options.applyHelpRequested(new PrintAndExit(0));
        options.applyVersionRequested(new PrintAndExit(0));
        options.applyErrors(new PrintAndExit(System.err, 1));

        RuntimeActions runtimeActions = new RuntimeActions();
        System.exit(runtimeActions.apply(options));
    }
}
