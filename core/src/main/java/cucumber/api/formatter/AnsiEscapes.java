package cucumber.api.formatter;

public class AnsiEscapes {
    private static final char ESC = 27;
    private static final char BRACKET = '[';

    public static AnsiEscapes RESET = color(0);
    public static AnsiEscapes BLACK = color(30);
    public static AnsiEscapes RED = color(31);
    public static AnsiEscapes GREEN = color(32);
    public static AnsiEscapes YELLOW = color(33);
    public static AnsiEscapes BLUE = color(34);
    public static AnsiEscapes MAGENTA = color(35);
    public static AnsiEscapes CYAN = color(36);
    public static AnsiEscapes WHITE = color(37);
    public static AnsiEscapes DEFAULT = color(9);
    public static AnsiEscapes GREY = color(90);
    public static AnsiEscapes INTENSITY_BOLD = color(1);

    private static AnsiEscapes color(int code) {
        return new AnsiEscapes(String.valueOf(code) + "m");
    }

    public static AnsiEscapes up(int count) {
        return new AnsiEscapes(String.valueOf(count) + "A");
    }

    private final String value;

    private AnsiEscapes(String value) {
        this.value = value;
    }

    public void appendTo(NiceAppendable a) {
        a.append(ESC).append(BRACKET).append(value);
    }

    public void appendTo(StringBuilder a) {
        a.append(ESC).append(BRACKET).append(value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        appendTo(sb);
        return sb.toString();
    }
}
