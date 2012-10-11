package cucumber.runtime.io;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class FlatteningIterator implements Iterator {
    private final Deque<Iterator<?>> iterators = new ArrayDeque<Iterator<?>>();

    private Object next;
    private boolean nextBlank = true;

    public void push(Iterator<?> iterator) {
        iterators.addFirst(iterator);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void moveToNext() {
        if (nextBlank && !this.iterators.isEmpty()) {
            if (!iterators.peek().hasNext()) {
                iterators.removeFirst();
                moveToNext();
            } else {
                final Object next = iterators.peekFirst().next();
                if (next instanceof Iterator) {
                    push((Iterator<?>) next);
                    moveToNext();
                } else {
                    this.next = next;
                    nextBlank = false;
                }
            }
        }
    }

    @Override
    public Object next() {
        moveToNext();

        if (nextBlank) {
            throw new NoSuchElementException();
        } else {
            Object next = this.next;
            this.next = null;
            nextBlank = true;
            return next;
        }
    }

    @Override
    public boolean hasNext() {
        moveToNext();
        return !nextBlank;
    }
}