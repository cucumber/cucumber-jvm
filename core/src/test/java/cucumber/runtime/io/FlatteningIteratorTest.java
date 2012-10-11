package cucumber.runtime.io;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class FlatteningIteratorTest {
    @Test
    public void flattens_iterators() {
        final FlatteningIterator fi = new FlatteningIterator();
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

    private List toList(final Iterator fi) {
        Iterable i = new Iterable() {
            @Override
            public Iterator iterator() {
                return fi;
            }
        };

        List l = new ArrayList();
        for (Object o : i) {
            l.add(o);
        }
        return l;
    }
}
