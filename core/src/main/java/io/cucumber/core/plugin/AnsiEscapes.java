package io.cucumber.core.plugin;

final class AnsiEscapes {
    static final AnsiEscapes RESET = color(0);
    static final AnsiEscapes BLACK = color(30);
    static final AnsiEscapes RED = color(31);
    static final AnsiEscapes GREEN = color(32);
    static final AnsiEscapes YELLOW = color(33);
    static final AnsiEscapes BLUE = color(34);
    static final AnsiEscapes MAGENTA = color(35);
    static final AnsiEscapes CYAN = color(36);
    static final AnsiEscapes WHITE = color(37);
    static final AnsiEscapes DEFAULT = color(9);
    static final AnsiEscapes GREY = color(90);
    static final AnsiEscapes INTENSITY_BOLD = color(1);
    static final AnsiEscapes UNDERLINE = color(4);
    private static final char ESC = 27;
    private static final char BRACKET = '[';
    private final String value;

    private AnsiEscapes(String value) {
        this.value = value;
    }

    private static AnsiEscapes color(int code) {
        return new AnsiEscapes(code + "m");
    }

    static AnsiEscapes up(int count) {
        return new AnsiEscapes(count + "A");
    }

    void appendTo(NiceAppendable a) {
        a.append(ESC).append(BRACKET).append(value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        appendTo(sb);
        return sb.toString();
    }

    void appendTo(StringBuilder a) {
        a.append(ESC).append(BRACKET).append(value);
    }

}
