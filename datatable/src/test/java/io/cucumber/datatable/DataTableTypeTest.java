package io.cucumber.datatable;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DataTableTypeTest {

    private final DataTableType singleCellType = new DataTableType(
        Integer.class, (@Nullable String s) -> Integer.parseInt(s));

    @Test
    void shouldTransformATableCell() {
        assertThat(singleCellType.transform(singletonList(singletonList("12"))),
            equalTo(singletonList(singletonList(12))));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldTransformATableEntry() {
        DataTableType tableType = new DataTableType(
            Place.class,
            (Map<String, String> entry) -> new Place(requireNonNull(entry.get("place"))));

        String here = "here";
        List<Place> transform = (List<Place>) tableType
                .transform(Arrays.asList(singletonList("place"), singletonList(here)));

        assertNotNull(transform);
        assertEquals(1, transform.size());
        assertEquals(here, transform.get(0).name);
    }

    @Test
    void shouldHaveAReasonableCanonicalRepresentation() {
        assertThat(singleCellType.toCanonical(), is("java.util.List<java.util.List<java.lang.Integer>>"));
    }

}
