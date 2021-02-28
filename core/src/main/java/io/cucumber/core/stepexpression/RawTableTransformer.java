package io.cucumber.core.stepexpression;

import java.util.List;

@FunctionalInterface
interface RawTableTransformer<T> {

    T transform(List<List<String>> raw);

}
