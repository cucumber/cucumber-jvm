package cucumber.api.datatable;

import java.util.List;

public interface RawTableTransformer<T> {
    T transform(List<List<String>> raw) throws Throwable;
}
