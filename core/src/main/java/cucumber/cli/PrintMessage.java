package cucumber.cli;

import java.io.PrintStream;

public class PrintMessage implements Messagable {
    private PrintStream _stream = System.out;
    private int _lineCount = 0;

    public PrintMessage() {
        init(_stream);
    }

    public PrintMessage(PrintStream $stream) {
        init($stream);
    }

    protected void init(PrintStream $stream) {
        _stream = $stream;
    }

    public void message(String $message) {
        _lineCount++;
        _stream.println($message);
    }
    
    public int lineCount() { return _lineCount; }
}
