package cucumber.runtime.io;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class FlatteningIteratorTest {
    @Test
    public void flattens_iterators() {
        final FlatteningIterator<Integer> fi = new FlatteningIterator<Integer>();
        fi.push(asList(3, 4).iterator());
        fi.push(asList(1, 2).iterator());

        assertEquals(asList(1, 2, 3, 4), toList(fi));
        assertFalse(fi.hasNext());

        try {
            fi.next();
            fail();
        } catch (NoSuchElementException expected) {
        }
    }

    private <T> List<T> toList(final Iterator<T> fi) {
        Iterable<T> i = new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return fi;
            }
        };

        List<T> l = new ArrayList<T>();
        for (T o : i) {
            l.add(o);
        }
        return l;
    }
}
