package cucumber.runtime.formatter;

public final class MonochromeFormats implements Formats {
    private static final Format FORMAT = new Format() {
        public String text(String text) {
            return text;
        }
    };

    public Format get(String key) {
        return FORMAT;
    }

    public String up(int n) {
        return "";
    }
}
