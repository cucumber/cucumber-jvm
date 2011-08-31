package cucumber.table;

/**
 * Default implementation of {@link TableHeaderMapper} which returns the original header name
 */
public class NoOpTableHeaderMapper implements TableHeaderMapper {

    @Override
    public String map(String originalHeaderName) {
        return originalHeaderName;
    }

}
