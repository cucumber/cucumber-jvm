package cucumber.runtime.io;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class TestResourceIterator implements Iterator<Resource> {

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Resource next() {
        throw new NoSuchElementException();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
