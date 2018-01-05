package cucumber.api.datatable;

public interface TableCellTransformer<T> {
    T transform(String cell);
}
