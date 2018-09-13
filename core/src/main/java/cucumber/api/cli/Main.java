package cucumber.api.cli;

/**
 * @deprecated use {@link io.cucumber.core.api.cli.Main}
 */
@Deprecated
public class Main {


    public static void main(String[] argv) {
        System.err.println("You are using deprecated Main method. Please use io.cucumber.core.api.cli.Main");
        io.cucumber.core.api.cli.Main.main(argv);
    }
}
