package io.cucumber.core.order;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.plugin.event.Location;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PickleOrderTest {

    @Mock
    Pickle firstPickle;

    @Mock
    Pickle secondPickle;

    @Mock
    Pickle thirdPickle;

    @Test
    void lexical_uri_order() {
        when(firstPickle.getUri()).thenReturn(URI.create("file:com/example/a.feature"));
        when(firstPickle.getLocation()).thenReturn(new Location(2, -1));
        when(secondPickle.getUri()).thenReturn(URI.create("file:com/example/a.feature"));
        when(secondPickle.getLocation()).thenReturn(new Location(3, -1));
        when(thirdPickle.getUri()).thenReturn(URI.create("file:com/example/b.feature"));

        PickleOrder order = StandardPickleOrders.lexicalUriOrder();
        List<Pickle> pickles = order.orderPickles(Arrays.asList(thirdPickle, secondPickle, firstPickle));
        assertThat(pickles, contains(firstPickle, secondPickle, thirdPickle));
    }

    @Test
    void reverse_lexical_uri_order() {
        when(firstPickle.getUri()).thenReturn(URI.create("file:com/example/a.feature"));
        when(firstPickle.getLocation()).thenReturn(new Location(2, -1));
        when(secondPickle.getUri()).thenReturn(URI.create("file:com/example/a.feature"));
        when(secondPickle.getLocation()).thenReturn(new Location(3, -1));
        when(thirdPickle.getUri()).thenReturn(URI.create("file:com/example/b.feature"));

        PickleOrder order = StandardPickleOrders.reverseLexicalUriOrder();
        List<Pickle> pickles = order.orderPickles(Arrays.asList(secondPickle, thirdPickle, firstPickle));
        assertThat(pickles, contains(thirdPickle, secondPickle, firstPickle));
    }

    @Test
    void random_order() {
        PickleOrder order = StandardPickleOrders.random(42);
        List<Pickle> pickles = order.orderPickles(Arrays.asList(firstPickle, secondPickle, thirdPickle));
        assertThat(pickles, contains(secondPickle, firstPickle, thirdPickle));
    }

}
