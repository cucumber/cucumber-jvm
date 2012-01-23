package cucumber.cli;

import java.io.PrintStream;

public class PrintAndExit implements Messagable {
    private PrintStream _stream = System.out;
    private int _statusCode;

    public PrintAndExit(int $statusCode) {
        init($statusCode, _stream);
    }

    public PrintAndExit(PrintStream $stream, int $statusCode) {
        init($statusCode, $stream);
    }

    protected void init(int $statusCode, PrintStream $stream) {
        _statusCode = $statusCode;
        _stream = $stream;
    }

    public void message(String $message) {
        _stream.println($message);
        System.exit(_statusCode);
    }
}
