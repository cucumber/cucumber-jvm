package cucumber.java.connectors.wire;

import cucumber.java.CucumberOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Socket server that calls a protocol handler line by line
 */
public class SocketServer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private ProtocolHandler protocolHandler;
    private ServerSocket serverSocket;
    private Socket socket;

    /**
     * Constructor for DI
     */
    public SocketServer(ProtocolHandler protocolHandler) {
        this.protocolHandler = protocolHandler;
    }

    /**
     * Bind and listen to a TCP port
     */
    public void listen(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    /**
     * Accept one connection
     */
    public void acceptOnce() throws Throwable {
        socket = serverSocket.accept();
        processStream(socket.getInputStream(), socket.getOutputStream());
        socket.close();
    }

    private void processStream(InputStream inputStream, OutputStream outputStream) throws Throwable {
        Scanner input = new Scanner(inputStream);
        PrintWriter output = new PrintWriter(outputStream);
        while (input.hasNextLine()) {
            String request = input.nextLine();
            if (CucumberOptions.getOptions().isVerbose()) {
                logger.debug("request " + request);
            }
            String response = protocolHandler.handle(request);
            if (CucumberOptions.getOptions().isVerbose()) {
                logger.debug("response " + response);
            }
            output.println(response);
            output.flush();
        }
    }
}
