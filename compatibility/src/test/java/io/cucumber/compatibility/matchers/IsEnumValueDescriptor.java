package io.cucumber.compatibility.matchers;

import io.cucumber.messages.internal.com.google.protobuf.Descriptors.EnumValueDescriptor;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

class IsEnumValueDescriptor extends TypeSafeDiagnosingMatcher<EnumValueDescriptor> {

    private final EnumValueDescriptor expected;

    public IsEnumValueDescriptor(EnumValueDescriptor expected) {
        this.expected = expected;
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(expected);
    }

    @Override
    protected boolean matchesSafely(EnumValueDescriptor item, Description mismatchDescription) {
        if (!expected.getName().equals(item.getName())) {
            mismatchDescription.appendValue(item);
            return false;
        }
        return true;
    }

}
