package cucumber.api.datatable;

import java.util.List;

public interface TableRowTransformer<T> {
    T transform(List<String> tableRow);
}
