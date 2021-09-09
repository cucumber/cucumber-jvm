package io.cucumber.core.stepexpression;

@FunctionalInterface
interface DocStringTransformer<T> {

    T transform(String docString, String contentType);

}
