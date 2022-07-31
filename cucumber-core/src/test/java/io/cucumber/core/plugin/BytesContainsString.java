package io.cucumber.core.plugin;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;

import java.io.ByteArrayOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

final class BytesContainsString {

    static DiagnosingMatcher<ByteArrayOutputStream> bytesContainsString(String expected) {
        return new DiagnosingMatcher<ByteArrayOutputStream>() {
            @Override
            protected boolean matches(Object actual, Description description) {
                description.appendText("was ");
                if (!(actual instanceof ByteArrayOutputStream)) {
                    description.appendValue(actual.getClass());
                    return false;
                }
                String actualString = new String(((ByteArrayOutputStream) actual).toByteArray(), UTF_8);
                description.appendValue(actualString);
                return actualString.contains(expected);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("bytes containing ");
                description.appendValue(expected);
            }
        };
    }

}
