package io.cucumber.junit;

import org.junit.experimental.categories.IncludeCategories;
import org.junit.runner.Description;
import org.junit.runner.FilterFactory;
import org.junit.runner.FilterFactoryParams;
import org.junit.runner.manipulation.Filter;

import java.lang.annotation.Annotation;

public class CategoryFilterFactory {
    public static Filter includeCategory(Class category, Annotation...annotations) throws FilterFactory.FilterNotCreatedException {
        Description description = Description.createTestDescription(CategoryFilterFactory.class, "CategoryFilterFactory", annotations);
        return new IncludeCategories().createFilter(new FilterFactoryParams(description, category.getName()));
    }
}
