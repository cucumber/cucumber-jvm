package cucumber.runtime.junit.categories;

import org.junit.experimental.categories.IncludeCategories;
import org.junit.runner.Description;
import org.junit.runner.FilterFactory;
import org.junit.runner.FilterFactoryParams;
import org.junit.runner.manipulation.Filter;

public class CategoryFilterFactory {

    public static Filter includeCategory(Class category) throws FilterFactory.FilterNotCreatedException {
        Description description = Description.createTestDescription(CategoryFilterFactory.class, "CategoryFilterFactory");
        return new IncludeCategories().createFilter(new FilterFactoryParams(description, category.getName()));
    }
}
