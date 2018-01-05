package cucumber.api.datatable;

import java.util.Map;

public interface TableEntryTransformer<T> {
    T transform(Map<String, String> tableEntry);
}
