package cucumber.runtime.java;

import cucumber.runtime.CucumberException;
import cucumber.runtime.JdkPatternArgumentMatcher;
import cucumber.runtime.StepDefinition;
import cucumber.table.Table;
import cucumber.table.TableConverter;
import cucumber.table.java.JavaBeanPropertyHeaderMapper;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Row;
import gherkin.formatter.model.Step;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

public class JavaStepDefinition implements StepDefinition {
    private final MethodFormat methodFormat;
    private final Method method;
    private final ObjectFactory objectFactory;
    private final JdkPatternArgumentMatcher argumentMatcher;
    private final Pattern pattern;
    private final JavaBeanPropertyHeaderMapper mapper = new JavaBeanPropertyHeaderMapper();

    public JavaStepDefinition(Pattern pattern, Method method, ObjectFactory objectFactory) {
        this.pattern = pattern;
        this.argumentMatcher = new JdkPatternArgumentMatcher(pattern);
        this.method = method;
        this.objectFactory = objectFactory;
        this.methodFormat = new MethodFormat();
    }

    public void execute(Object[] args) throws Throwable {
        Object target = objectFactory.getInstance(method.getDeclaringClass());
        try {
            method.invoke(target, args);
        } catch (IllegalArgumentException e) {
            // Can happen if stepdef signature doesn't match args
            throw new CucumberException("Can't invoke " + new MethodFormat().format(method) + " with " + asList(args));
        }
    }

    public List<Argument> matchedArguments(Step step) {
        return argumentMatcher.argumentsFrom(step.getName());
    }

    public String getLocation() {
        return methodFormat.format(method);
    }

    public Class<?>[] getParameterTypes() {
        return method.getParameterTypes();
    }

    public boolean isDefinedAt(StackTraceElement e) {
        return e.getClassName().equals(method.getDeclaringClass().getName()) && e.getMethodName().equals(method.getName());
    }

    @Override
    public String getPattern() {
        return pattern.pattern();
    }

    @Override
    public Object tableArgument(int argIndex, List<Row> rows, TableConverter tableConverter) {
        Type genericParameterType = method.getGenericParameterTypes()[argIndex];
        if (genericParameterType instanceof ParameterizedType) {
            Type[] parameters = ((ParameterizedType) genericParameterType).getActualTypeArguments();
            Class<?> itemType = (Class<?>) parameters[0];
            return tableConverter.convert(itemType, attributeNames(rows), attributeValues(rows));
        } else {
            return new Table(rows);
        }
    }

    private List<List<String>> attributeValues(List<Row> rows) {
        List<List<String>> attributeValues = new ArrayList<List<String>>();
        List<Row> valueRows = rows.subList(1, rows.size());
        for (Row valueRow : valueRows) {
            attributeValues.add(toStrings(valueRow));
        }
        return attributeValues;
    }

    private List<String> attributeNames(List<Row> rows) {
        List<String> strings = new ArrayList<String>();
        for (String string : rows.get(0).getCells()) {
            strings.add(mapper.map(string));
        }
        return strings;
    }

    private List<String> toStrings(Row row) {
        List<String> strings = new ArrayList<String>();
        for (String string : row.getCells()) {
            strings.add(string);
        }
        return strings;
    }
}
