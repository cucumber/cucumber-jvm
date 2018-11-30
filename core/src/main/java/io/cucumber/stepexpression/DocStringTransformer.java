package io.cucumber.stepexpression;


interface DocStringTransformer<T> {
    T transform(String docString);
}
