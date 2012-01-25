package cucumber.cli;

import java.io.PrintStream;

public class PrintMessage implements Messagable {
    private PrintStream stream = System.out;
    private int lineCount = 0;

    public PrintMessage() {
        init(stream);
    }

    public PrintMessage(PrintStream $stream) {
        init($stream);
    }

    protected void init(PrintStream $stream) {
        stream = $stream;
    }

    public void message(String message) {
        lineCount++;
        stream.println(message);
    }
    
    public int lineCount() { return lineCount; }
}
