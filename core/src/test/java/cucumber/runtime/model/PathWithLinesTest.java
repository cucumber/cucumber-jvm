package cucumber.runtime.model;

import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

public class PathWithLinesTest {
    @Test
    public void should_create_FileWithFilters_with_no_lines() {
        PathWithLines pathWithLines = new PathWithLines("foo.feature");
        assertEquals("foo.feature", pathWithLines.path);
        assertEquals(emptyList(), pathWithLines.lines);
    }

    @Test
    public void should_create_FileWithFilters_with_2_lines() {
        PathWithLines pathWithLines = new PathWithLines("foo.feature:999:2000");
        assertEquals("foo.feature", pathWithLines.path);
        assertEquals(asList(999L, 2000L), pathWithLines.lines);
    }

    @Test
    public void should_create_FileWithFilters_with_2_lines_and_windows_path() {
        PathWithLines pathWithLines = new PathWithLines("C:\\bar\\foo.feature:999:2000");
        assertEquals("C:\\bar\\foo.feature", pathWithLines.path);
        assertEquals(asList(999L, 2000L), pathWithLines.lines);
    }
}
