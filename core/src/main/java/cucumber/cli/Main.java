package cucumber.cli;

import com.beust.jcommander.JCommander;

import cucumber.runtime.Runtime;

public class Main {
    private static final String VERSION = "1.0.0"; // TODO: get this from a file

    public static void main(String[] argv) {
        CliArgs args = new CliArgs();
        JCommander jCommander = new JCommander(args, argv);
        if (args.showUsage) {
            jCommander.usage();
            System.exit(0);
        }
        if (args.showVersion) {
            System.out.println("Cucumber " + VERSION);
            System.exit(0);
        }
        // if packageNameOrScriptPrefix is marked as required, will throw exception if main is called with --help ou --version only
        if (args.packageNameOrScriptPrefix == null) {
            System.out.println("Missing option: --glue");
            System.exit(0);
        }
        Runtime runtime = null;        
        runtime = new Runtime(args.packageNameOrScriptPrefix);
        Cli cli = new Cli(runtime, args.filesOrDirs);
        cli.run();
    }
}
