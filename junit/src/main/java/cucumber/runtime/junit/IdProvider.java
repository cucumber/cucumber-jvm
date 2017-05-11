package cucumber.runtime.junit;

import java.io.Serializable;

/**
 * JUnit Descriptions require an unique id. However this id should be identical between test
 * re-runs. This allows descriptions to be used to determine which failed tests should be rerun.
 *
 * IdProvider generates predictable unique ids. The feature name is used as a base to disambiguate
 * between different features.
 */
class IdProvider {

    private long id = 0;
    private final String base;

    IdProvider(String base) {
        this.base = base;
    }

    Serializable next() {
        return new Id(base, id++);
    }

    private static final class Id implements Serializable {

        private final String base;
        private final long id;

        private Id(String base, long id) {
            this.base = base;
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Id id1 = (Id) o;
            return id == id1.id && base.equals(id1.base);

        }

        @Override
        public int hashCode() {
            int result = base.hashCode();
            result = 31 * result + (int) (id ^ (id >>> 32));
            return result;
        }
    }
}




