package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.cucumberexpressions.Argument;
import io.cucumber.cucumberexpressions.CucumberExpression;
import io.cucumber.cucumberexpressions.CucumberExpressionException;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JavaParameterTypeDefinitionTest {

    private final Lookup lookup = new Lookup() {
        @Override
        @SuppressWarnings("unchecked")
        public <T> T getInstance(Class<T> glueClass) {
            return (T) JavaParameterTypeDefinitionTest.this;
        }
    };

    private final ParameterTypeRegistry registry = new ParameterTypeRegistry(Locale.ENGLISH);

    @Test
    public void can_define_parameter_type_converters_with_one_capture_group() throws NoSuchMethodException {
        Method method = JavaParameterTypeDefinitionTest.class.getMethod("convert_one_capture_group_to_string", String.class);
        JavaParameterTypeDefinition definition = new JavaParameterTypeDefinition("", "(.*)", method, false, false, lookup);
        registry.defineParameterType(definition.parameterType());
        CucumberExpression cucumberExpression = new CucumberExpression("{convert_one_capture_group_to_string}", registry);
        List<Argument<?>> test = cucumberExpression.match("test");
        assertThat(test.get(0).getValue(), equalTo("convert_one_capture_group_to_string"));
    }

    public String convert_one_capture_group_to_string(String all) {
        return "convert_one_capture_group_to_string";
    }

    @Test
    public void can_define_parameter_type_converters_with_two_capture_groups() throws NoSuchMethodException {
        Method method = JavaParameterTypeDefinitionTest.class.getMethod("convert_two_capture_group_to_string", String.class, String.class);
        JavaParameterTypeDefinition definition = new JavaParameterTypeDefinition("", "([^ ]*) ([^ ]*)", method, false, false, lookup);
        registry.defineParameterType(definition.parameterType());
        CucumberExpression cucumberExpression = new CucumberExpression("{convert_two_capture_group_to_string}", registry);
        List<Argument<?>> test = cucumberExpression.match("test test");
        assertThat(test.get(0).getValue(), equalTo("convert_two_capture_group_to_string"));
    }

    public String convert_two_capture_group_to_string(String captureGroup1, String captureGroup2) {
        return "convert_two_capture_group_to_string";
    }

    @Test
    public void can_define_parameter_type_converters_with_var_args() throws NoSuchMethodException {
        Method method = JavaParameterTypeDefinitionTest.class.getMethod("convert_varargs_capture_group_to_string", String[].class);
        JavaParameterTypeDefinition definition = new JavaParameterTypeDefinition("", "([^ ]*) ([^ ]*)", method, false, false, lookup);
        registry.defineParameterType(definition.parameterType());
        CucumberExpression cucumberExpression = new CucumberExpression("{convert_varargs_capture_group_to_string}", registry);
        List<Argument<?>> test = cucumberExpression.match("test test");
        assertThat(test.get(0).getValue(), equalTo("convert_varargs_capture_group_to_string"));
    }

    public String convert_varargs_capture_group_to_string(String... captureGroups) {
        return "convert_varargs_capture_group_to_string";
    }

    @Test
    public void arguments_must_match_captured_groups() throws NoSuchMethodException {
        Method method = JavaParameterTypeDefinitionTest.class.getMethod("convert_two_capture_group_to_string", String.class, String.class);
        JavaParameterTypeDefinition definition = new JavaParameterTypeDefinition("", ".*", method, false, false, lookup);
        registry.defineParameterType(definition.parameterType());
        CucumberExpression cucumberExpression = new CucumberExpression("{convert_two_capture_group_to_string}", registry);
        List<Argument<?>> test = cucumberExpression.match("test");
        assertThrows(CucumberExpressionException.class, () -> test.get(0).getValue());
    }


    @Test
    public void converter_must_have_return_type() throws NoSuchMethodException {
        Method method = JavaParameterTypeDefinitionTest.class.getMethod("convert_capture_group_to_void", String.class);
        assertThrows(InvalidMethodSignatureException.class, () -> new JavaParameterTypeDefinition("", "(.*)", method, false, false, lookup));
    }

    public void convert_capture_group_to_void(String all) {
    }

    @Test
    public void converter_must_have_non_generic_return_type() throws NoSuchMethodException {
        Method method = JavaParameterTypeDefinitionTest.class.getMethod("convert_capture_group_to_optional_string", String.class);
        assertThrows(InvalidMethodSignatureException.class, () -> new JavaParameterTypeDefinition("", "(.*)", method, false, false, lookup));
    }

    public Optional<String> convert_capture_group_to_optional_string(String all) {
        return Optional.of("convert_capture_group_to_optional_string");
    }

    @Test
    public void converter_must_have_at_least_one_argument() throws NoSuchMethodException {
        Method method = JavaParameterTypeDefinitionTest.class.getMethod("convert_nothing_to_string");
        assertThrows(InvalidMethodSignatureException.class, () -> new JavaParameterTypeDefinition("", "(.*)", method, false, false, lookup));
    }

    public String convert_nothing_to_string() {
        return "convert_nothing_to_string";
    }

    @Test
    public void converter_must_have_string_arguments() throws NoSuchMethodException {
        Method method = JavaParameterTypeDefinitionTest.class.getMethod("converts_object_to_string", Object.class);
        assertThrows(InvalidMethodSignatureException.class, () -> new JavaParameterTypeDefinition("", "(.*)", method, false, false, lookup));
    }

    public String converts_object_to_string(Object other) {
        return "converts_object_to_string";
    }

    @Test
    public void converter_must_have_all_string_arguments() throws NoSuchMethodException {
        Method method = JavaParameterTypeDefinitionTest.class.getMethod("converts_objects_to_string", String.class, Object.class);
        assertThrows(InvalidMethodSignatureException.class, () -> new JavaParameterTypeDefinition("", "(.*)", method, false, false, lookup));
    }

    public String converts_objects_to_string(String all, Object other) {
        return "converts_object_to_string";
    }

}