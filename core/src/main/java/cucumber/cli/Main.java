package cucumber.cli;

public class Main {
    public static void main(String[] argv) throws Throwable {
        // TODO: need to wrap this in some tests and mock out System.exit and System.out. Should probably look at slf4j.
        RuntimeOptions options = new RuntimeOptions();

        options.parse(argv);

        if(options.isHelpRequested()) {
            System.out.println(options.usage());
            System.exit(0); // report success?
        }

        if(options.isVersionRequested()) {
            System.out.println(options.version());
            System.exit(0); // report success?
        }

        if(options.hasErrors()) {
            for(String error : options.getErrors()) {
                System.err.println(error);
            }

            System.exit(1);
        }


        RuntimeActions runtimeActions = new RuntimeActions();
        System.exit(runtimeActions.apply(options));
    }
}
