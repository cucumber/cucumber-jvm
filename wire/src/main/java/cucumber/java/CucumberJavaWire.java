package cucumber.java;

import cucumber.java.connectors.wire.SocketServer;

import cucumber.java.connectors.wire.WireProtocolHandler;
import cucumber.java.connectors.wire.gson.GsonWireMessageCodec;
import cucumber.java.step.CucumberJavaStepsLoader;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main entry point for a Java-Wire application.
 */
public class CucumberJavaWire {
    private static final Logger logger = LoggerFactory.getLogger(CucumberJavaWire.class);

    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addOption("h", "help", false, "help for Cucumber Java Wire");
        options.addOption("v", "verbose", false, "verbose output");
        options.addOption("p", "port", true, "listening port of wireserver");

        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("h") || line.hasOption("help")) {
                new HelpFormatter().printHelp("CucumberJavaWire", options);
            } else {
                if (line.hasOption("verbose")) {
                    CucumberOptions.getOptions().setVerbose(true);
                }

                if (line.hasOption("port")) {
                    CucumberOptions.getOptions().setPort(Integer.parseInt(line.getOptionValue("port")));
                }

                acceptWireProtocol();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            new HelpFormatter().printHelp("CucumberJavaWire", options);
        }
    }

    private static void acceptWireProtocol() throws Throwable {

        CucumberJavaStepsLoader loader = new CucumberJavaStepsLoader();
        loader.loadSteps();

        CukeEngine cukeEngine = new CukeEngineImpl();
        GsonWireMessageCodec wireCodec = new GsonWireMessageCodec();
        WireProtocolHandler protocolHandler = new WireProtocolHandler(wireCodec, cukeEngine);
        SocketServer server = new SocketServer(protocolHandler);
        server.listen(CucumberOptions.getOptions().getPort());
        if (CucumberOptions.getOptions().isVerbose()) {
            logger.debug("Listening on port " + CucumberOptions.getOptions().getPort());
        }
        server.acceptOnce();
    }

}
