package cucumber.api.datatable;


public interface TableTransformer<T> {
    T transform(DataTable table);
}
