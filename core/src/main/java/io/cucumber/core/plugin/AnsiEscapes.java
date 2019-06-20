package io.cucumber.core.plugin;

final class AnsiEscapes {
    private static final char ESC = 27;
    private static final char BRACKET = '[';

    static AnsiEscapes RESET = color(0);
    static AnsiEscapes BLACK = color(30);
    static AnsiEscapes RED = color(31);
    static AnsiEscapes GREEN = color(32);
    static AnsiEscapes YELLOW = color(33);
    static AnsiEscapes BLUE = color(34);
    static AnsiEscapes MAGENTA = color(35);
    static AnsiEscapes CYAN = color(36);
    static AnsiEscapes WHITE = color(37);
    static AnsiEscapes DEFAULT = color(9);
    static AnsiEscapes GREY = color(90);
    static AnsiEscapes INTENSITY_BOLD = color(1);

    private static AnsiEscapes color(int code) {
        return new AnsiEscapes(code + "m");
    }

    static AnsiEscapes up(int count) {
        return new AnsiEscapes(count + "A");
    }

    private final String value;

    private AnsiEscapes(String value) {
        this.value = value;
    }

    void appendTo(NiceAppendable a) {
        a.append(ESC).append(BRACKET).append(value);
    }

    void appendTo(StringBuilder a) {
        a.append(ESC).append(BRACKET).append(value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        appendTo(sb);
        return sb.toString();
    }
}
