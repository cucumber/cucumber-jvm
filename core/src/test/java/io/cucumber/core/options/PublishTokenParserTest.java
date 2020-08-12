package io.cucumber.core.options;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PublishTokenParserTest {

    @Test
    public void parses_base64() {
        String token = Base64.getEncoder().encodeToString("token".getBytes(UTF_8));
        assertDoesNotThrow(() -> PublishTokenParser.parse(token));
    }

    @Test
    public void throws_for_non_base64() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PublishTokenParser.parse("!@#$%^&*()_"));

        assertThat(exception.getMessage(),
            CoreMatchers.is("Invalid token. A token must consist of a RFC4648 Base64 encoded string"));
    }

}
