package io.cucumber.core.filter;

import gherkin.events.PickleEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FiltersTest {

    @Mock
    private Options fomock;

    private Filters filters;

    private List<PickleEvent> pickles = createFilteredPickleList(8);

    @Test
    public void count_less_than_pickles_list_size() {
        when(fomock.getLimitCount()).thenReturn(5);
        filters = new Filters(fomock);
        assertThat(filters.limitPickleEvents(pickles).size(), is(5));
    }

    @Test
    public void count_greater_than_pickles_list_size() {
        when(fomock.getLimitCount()).thenReturn(10);
        filters = new Filters(fomock);
        assertThat(filters.limitPickleEvents(pickles).size(), is(8));
    }

    @Test
    public void default_count_zero() {
        when(fomock.getLimitCount()).thenReturn(0);
        filters = new Filters(fomock);
        assertThat(filters.limitPickleEvents(pickles).size(), is(8));
    }

    private List<PickleEvent> createFilteredPickleList(int size) {
        List<PickleEvent> pickles = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            pickles.add(mock(PickleEvent.class));
        }
        return pickles;
    }

}
