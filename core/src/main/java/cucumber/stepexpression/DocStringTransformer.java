package cucumber.stepexpression;


interface DocStringTransformer<T> {
    T transform(String table);
}
