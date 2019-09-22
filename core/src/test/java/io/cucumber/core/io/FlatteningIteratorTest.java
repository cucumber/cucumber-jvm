package io.cucumber.core.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FlatteningIteratorTest {

    @Test
    void flattens_iterators() {
        final FlatteningIterator<Integer> fi = new FlatteningIterator<>();
        fi.push(asList(3, 4).iterator());
        fi.push(asList(1, 2).iterator());

        assertThat(toList(fi), is(equalTo(asList(1, 2, 3, 4))));
        assertFalse(fi.hasNext());

        final Executable testMethod = fi::next;
        final NoSuchElementException actualThrown = assertThrows(NoSuchElementException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(nullValue()));
    }

    private <T> List<T> toList(final Iterator<T> fi) {
        Iterable<T> i = () -> fi;

        List<T> l = new ArrayList<>();
        for (T o : i) {
            l.add(o);
        }
        return l;
    }

}
