package io.cucumber.core.order;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.gherkin.Step;
import io.cucumber.plugin.event.Location;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

class PickleOrderTest {

    @Test
    void lexical_uri_order() {
        Pickle firstPickle = new MockPickle(new Location(2, -1), URI.create("file:com/example/a.feature"));
        Pickle secondPickle = new MockPickle(new Location(3, -1), URI.create("file:com/example/a.feature"));
        Pickle thirdPickle = new MockPickle(null, URI.create("file:com/example/b.feature"));

        PickleOrder order = StandardPickleOrders.lexicalUriOrder();
        List<Pickle> pickles = order.orderPickles(Arrays.asList(thirdPickle, secondPickle, firstPickle));
        assertThat(pickles, contains(firstPickle, secondPickle, thirdPickle));
    }

    @Test
    void reverse_lexical_uri_order() {
        Pickle firstPickle = new MockPickle(new Location(2, -1), URI.create("file:com/example/a.feature"));
        Pickle secondPickle = new MockPickle(new Location(3, -1), URI.create("file:com/example/a.feature"));
        Pickle thirdPickle = new MockPickle(null, URI.create("file:com/example/b.feature"));

        PickleOrder order = StandardPickleOrders.reverseLexicalUriOrder();
        List<Pickle> pickles = order.orderPickles(Arrays.asList(secondPickle, thirdPickle, firstPickle));
        assertThat(pickles, contains(thirdPickle, secondPickle, firstPickle));
    }

    @Test
    void random_order() {
        Pickle firstPickle = new MockPickle(new Location(2, -1), URI.create("file:com/example/a.feature"));
        Pickle secondPickle = new MockPickle(new Location(3, -1), URI.create("file:com/example/a.feature"));
        Pickle thirdPickle = new MockPickle(null, URI.create("file:com/example/b.feature"));

        PickleOrder order = StandardPickleOrders.random(42);
        List<Pickle> pickles = order.orderPickles(Arrays.asList(firstPickle, secondPickle, thirdPickle));
        assertThat(pickles, contains(secondPickle, firstPickle, thirdPickle));
    }

    private static class MockPickle implements Pickle {
        private final Location location;
        private final URI uri;

        public MockPickle(Location location, URI uri) {
            this.location = location;
            this.uri = uri;
        }

        @Override
        public String getKeyword() {
            return null;
        }

        @Override
        public String getLanguage() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public Location getLocation() {
            return location;
        }

        @Override
        public Location getScenarioLocation() {
            return null;
        }

        @Override
        public List<Step> getSteps() {
            return null;
        }

        @Override
        public List<String> getTags() {
            return null;
        }

        @Override
        public URI getUri() {
            return uri;
        }

        @Override
        public String getId() {
            return null;
        }
    }
}
