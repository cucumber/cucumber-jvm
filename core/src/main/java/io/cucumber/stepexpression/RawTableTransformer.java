package io.cucumber.stepexpression;

import java.util.List;

interface RawTableTransformer<T> {
    T transform(List<List<String>> raw);
}
