package io.cucumber.core.plugin;

final class MonochromeFormats implements Formats {

    public Format get(String key) {
        return text -> text;
    }

    public String up(int n) {
        return "";
    }
}
