package io.cucumber.core.stepexpression;


interface DocStringTransformer<T> {
    T transform(String docString);
}
