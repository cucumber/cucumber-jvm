package io.cucumber.compatibility.matchers;

import io.cucumber.messages.internal.com.google.protobuf.ByteString;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class IsByteString extends TypeSafeDiagnosingMatcher<ByteString> {

    private final ByteString expected;

    public IsByteString(ByteString expected) {
        this.expected = expected;
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(expected.toStringUtf8());
    }

    @Override
    protected boolean matchesSafely(ByteString item, Description mismatchDescription) {
        String actual = item.toStringUtf8();
        if (this.expected.toStringUtf8().equals(actual)) {
            return true;
        }
        mismatchDescription.appendValue(actual);
        return false;
    }

}
