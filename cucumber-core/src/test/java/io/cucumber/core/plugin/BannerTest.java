package io.cucumber.core.plugin;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static io.cucumber.core.plugin.Bytes.bytes;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class BannerTest {

    @Test
    void printsAnsiBanner() throws UnsupportedEncodingException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Banner banner = new Banner(new PrintStream(bytes, false, StandardCharsets.UTF_8.name()), false);

        banner.print(asList(
            new Banner.Line("Bla"),
            new Banner.Line(
                new Banner.Span("Bla "),
                new Banner.Span("Bla", AnsiEscapes.BLUE),
                new Banner.Span(" "),
                new Banner.Span("Bla", AnsiEscapes.RED)),
            new Banner.Line("Bla Bla")), AnsiEscapes.CYAN);

        assertThat(bytes, bytes(equalTo("""
                [36m┌─────────────┐[0m
                [36m│[0m Bla         [36m│[0m
                [36m│[0m Bla [34mBla[0m [31mBla[0m [36m│[0m
                [36m│[0m Bla Bla     [36m│[0m
                [36m└─────────────┘[0m
                """)));
    }

    @Test
    void printsMonochromeBanner() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Banner banner = new Banner(new PrintStream(bytes, false, StandardCharsets.UTF_8.name()), true);

        banner.print(asList(
            new Banner.Line("Bla"),
            new Banner.Line(
                new Banner.Span("Bla "),
                new Banner.Span("Bla", AnsiEscapes.BLUE),
                new Banner.Span(" "),
                new Banner.Span("Bla", AnsiEscapes.RED)),
            new Banner.Line("Bla Bla")), AnsiEscapes.CYAN);

        assertThat(bytes, bytes(equalTo("""
                ┌─────────────┐
                │ Bla         │
                │ Bla Bla Bla │
                │ Bla Bla     │
                └─────────────┘
                """)));
    }

}
