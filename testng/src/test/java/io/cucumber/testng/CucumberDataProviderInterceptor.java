package io.cucumber.testng;

import org.testng.IDataProviderInterceptor;
import org.testng.IDataProviderMethod;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Predicate;

public class CucumberDataProviderInterceptor implements IDataProviderInterceptor {

    @Override
    public Iterator<Object[]> intercept(Iterator<Object[]> original, IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext) {
        if ("tag2Scenarios".equals(dataProviderMethod.getName())) {
            return filterScenarios(original, tags -> tags.contains("@TAG2")).iterator();
        } else {
            return filterScenarios(original, tags -> tags.contains("@TAG1")).iterator();
        }
    }

    private Collection<Object[]> filterScenarios(Iterator<Object[]> originalDataSet, Predicate<Collection<String>> tagsCondition) {
        Collection<Object[]> filteredDataSet = new HashSet<>();
        originalDataSet.forEachRemaining(data -> Arrays.stream(data).forEach(item -> {
            if (item instanceof PickleWrapper) {
                PickleWrapper pickleWrapper = (PickleWrapper) item;
                if (tagsCondition.test(pickleWrapper.getPickle().getTags())) {
                    filteredDataSet.add(data);
                }
            }
        }));
        return filteredDataSet;
    }
}
