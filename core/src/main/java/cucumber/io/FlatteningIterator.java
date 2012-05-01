package cucumber.io;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator that 'flattens out' collections, iterators, arrays, etc.
 * <p/>
 * That is it will iterate out their contents in order, descending into any
 * iterators, iterables or arrays provided to it.
 * <p/>
 * An example (not valid Java for brevity - some type declarations are ommitted):
 * <p/>
 * new FlattingIterator({1, 2, 3}, {{1, 2}, {3}}, new ArrayList({1, 2, 3}))
 * <p/>
 * Will iterate through the sequence 1, 2, 3, 1, 2, 3, 1, 2, 3.
 * <p/>
 * Note that this implements a non-generic version of the Iterator interface so
 * may be cast appropriately - it's very hard to give this class an appropriate
 * generic type.
 *
 * @author david
 */
public class FlatteningIterator implements Iterator {
    // Marker object. This is never exposed outside this class, so can be guaranteed
    // to be != anything else. We use it to indicate an absense of any other object.
    private final Object blank = new Object();

    /* This stack stores all the iterators found so far. The head of the stack is
* the iterator which we are currently progressing through */
    private final Deque<Iterator<?>> iterators = new ArrayDeque<Iterator<?>>();

    // Storage field for the next element to be returned. blank when the next element
    // is currently unknown.
    private Object next = blank;

    public FlatteningIterator(Object... objects) {
        Iterator<Object> iterator = Arrays.asList(objects).iterator();
        push(iterator);
    }

    public void push(Iterator<?> iterator) {
        iterators.addFirst(iterator);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void moveToNext() {
        if ((next == blank) && !this.iterators.isEmpty()) {
            if (!iterators.peek().hasNext()) {
                iterators.removeFirst();
                moveToNext();
            } else {
                final Object next = iterators.peekFirst().next();
                if (next instanceof Iterator) {
                    push((Iterator<?>) next);
                    moveToNext();
                } else if (next instanceof Iterable) {
                    push(((Iterable) next).iterator());
                    moveToNext();
                } else if (next instanceof Array) {
                    push(Arrays.asList((Array) next).iterator());
                    moveToNext();
                } else {
                    this.next = next;
                }
            }
        }
    }

    /**
     * Returns the next element in our iteration, throwing a NoSuchElementException
     * if none is found.
     */
    public Object next() {
        moveToNext();

        if (this.next == blank) {
            throw new NoSuchElementException();
        } else {
            Object next = this.next;
            this.next = blank;
            return next;
        }
    }

    /**
     * Returns if there are any objects left to iterate over. This method
     * can change the internal state of the object when it is called, but repeated
     * calls to it will not have any additional side effects.
     */
    public boolean hasNext() {
        moveToNext();
        return (this.next != blank);
    }
}