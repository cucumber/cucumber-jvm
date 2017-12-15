package cucumber.api.datatable;


public interface DocStringTransformer<T> {
    T transform(String table);
}
