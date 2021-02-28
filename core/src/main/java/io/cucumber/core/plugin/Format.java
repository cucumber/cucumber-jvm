package io.cucumber.core.plugin;

interface Format {

    String text(String text);

    static Format color(AnsiEscapes... escapes) {
        return new Color(escapes);
    }

    static Format monochrome() {
        return new Monochrome();
    }

    final class Color implements Format {

        private final AnsiEscapes[] escapes;

        private Color(AnsiEscapes... escapes) {
            this.escapes = escapes;
        }

        public String text(String text) {
            StringBuilder sb = new StringBuilder();
            for (AnsiEscapes escape : escapes) {
                escape.appendTo(sb);
            }
            sb.append(text);
            if (escapes.length > 0) {
                AnsiEscapes.RESET.appendTo(sb);
            }
            return sb.toString();
        }

    }

    class Monochrome implements Format {

        private Monochrome() {

        }

        @Override
        public String text(String text) {
            return text;
        }

    }

}
