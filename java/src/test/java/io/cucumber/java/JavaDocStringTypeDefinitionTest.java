package io.cucumber.java;

import com.fasterxml.jackson.core.type.TypeReference;
import io.cucumber.core.backend.Lookup;
import io.cucumber.docstring.DocString;
import io.cucumber.docstring.DocStringTypeRegistry;
import io.cucumber.docstring.DocStringTypeRegistryDocStringConverter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaDocStringTypeDefinitionTest {

    private static final Type MAP_OF_STRING_TO_MAP_OF_INTEGER_DOUBLE = new TypeReference<Map<String, Map<Integer, Double>>>() {
    }.getType();
    private static final Type MAP_OF_STRING_TO_MAP_OF_INTEGER_GREET = new TypeReference<Map<String, Map<Integer, Greet>>>() {
    }.getType();
    private static final Type MAP_OF_MEET_TO_MAP_OF_GREET_LEAVE = new TypeReference<Map<Meet, Map<Greet, Leave>>>() {
    }.getType();
    private static final Type MAP_OF_STRING_TO_LIST_OF_DOUBLE = new TypeReference<Map<String, List<Double>>>() {
    }.getType();
    private static final Type MAP_OF_STRING_TO_LIST_OF_LEAVE = new TypeReference<Map<String, List<Leave>>>() {
    }.getType();
    private static final Type LIST_OF_MAP_OF_STRING_TO_INT = new TypeReference<List<Map<String, Integer>>>() {
    }.getType();
    private static final Type LIST_OF_MAP_OF_STRING_MEET = new TypeReference<List<Map<String, Meet>>>() {
    }.getType();
    private static final Type LIST_OF_LIST_OF_INT = new TypeReference<List<List<Integer>>>() {
    }.getType();
    private static final Type LIST_OF_LIST_OF_GREET = new TypeReference<List<List<Greet>>>() {
    }.getType();
    private static final Type OPTIONAL_STRING = new TypeReference<Optional<String>>() {
    }.getType();
    public static final Type OPTIONAL_GREET_TYPE = new TypeReference<Optional<Greet>>() {
    }.getType();
    private static final Type LIST_OF_OPTIONAL_STRING = new TypeReference<List<Optional<String>>>() {
    }.getType();
    private static final Type LIST_OF_OPTIONAL_LEAVE = new TypeReference<List<Optional<Leave>>>() {
    }.getType();
    @SuppressWarnings("rawtypes")
    private static final Type LIST_OF_MAP = new TypeReference<List<Map>>() {
    }.getType();
    @SuppressWarnings("rawtypes")
    private static final Type LIST_OF_LIST = new TypeReference<List<List>>() {
    }.getType();
    @SuppressWarnings("rawtypes")
    private static final Type MAP_OF_STRING_TO_MAP = new TypeReference<Map<String, Map>>() {
    }.getType();
    private static final List<Type> TYPES = new ArrayList<>();

    static {
        TYPES.add(MAP_OF_STRING_TO_MAP_OF_INTEGER_DOUBLE);
        TYPES.add(MAP_OF_STRING_TO_MAP_OF_INTEGER_GREET);
        TYPES.add(MAP_OF_MEET_TO_MAP_OF_GREET_LEAVE);
        TYPES.add(MAP_OF_STRING_TO_LIST_OF_DOUBLE);
        TYPES.add(MAP_OF_STRING_TO_LIST_OF_LEAVE);
        TYPES.add(LIST_OF_MAP_OF_STRING_TO_INT);
        TYPES.add(LIST_OF_MAP_OF_STRING_MEET);
        TYPES.add(LIST_OF_LIST_OF_INT);
        TYPES.add(LIST_OF_LIST_OF_GREET);
        TYPES.add(OPTIONAL_STRING);
        TYPES.add(OPTIONAL_GREET_TYPE);
        TYPES.add(LIST_OF_OPTIONAL_STRING);
        TYPES.add(LIST_OF_OPTIONAL_LEAVE);

        TYPES.add(LIST_OF_MAP);
        TYPES.add(LIST_OF_LIST);
        TYPES.add(MAP_OF_STRING_TO_MAP);
    }

    private final Lookup lookup = new Lookup() {
        @Override
        @SuppressWarnings("unchecked")
        public <T> T getInstance(Class<T> glueClass) {
            return (T) JavaDocStringTypeDefinitionTest.this;
        }
    };

    private final DocString docString = DocString.create("some doc string", "text/plain");
    private final DocStringTypeRegistry registry = new DocStringTypeRegistry();
    private final DocStringTypeRegistryDocStringConverter converter = new DocStringTypeRegistryDocStringConverter(
        registry);

    @Test
    void doc_string_type_gets_correctly_init() throws NoSuchMethodException {
        Method method = JavaDocStringTypeDefinitionTest.class.getMethod("convert_doc_string_to_string", String.class);
        JavaDocStringTypeDefinition defaultContentType = new JavaDocStringTypeDefinition("", method, lookup);
        JavaDocStringTypeDefinition hasContentType = new JavaDocStringTypeDefinition("text/plain", method, lookup);

        assertThat(defaultContentType.docStringType().getContentType(),
            is("convert_doc_string_to_string"));
        assertThat(defaultContentType.docStringType().getType(), instanceOf(Object.class));
        assertThat(hasContentType.docStringType().getContentType(),
            is("text/plain"));
        assertThat(hasContentType.docStringType().getType(), instanceOf(Object.class));
    }

    @Test
    void can_define_doc_string_converter() throws NoSuchMethodException {
        Method method = JavaDocStringTypeDefinitionTest.class.getMethod("convert_doc_string_to_string", String.class);
        JavaDocStringTypeDefinition definition = new JavaDocStringTypeDefinition("text/plain", method, lookup);
        registry.defineDocStringType(definition.docStringType());
        assertThat(converter.convert(docString, Object.class), is("some_desired_string"));
    }

    @Test
    void can_define_doc_string_without_content_types_converter() throws NoSuchMethodException {
        Method method = JavaDocStringTypeDefinitionTest.class.getMethod("convert_doc_string_to_string", String.class);
        JavaDocStringTypeDefinition definition = new JavaDocStringTypeDefinition("", method, lookup);
        registry.defineDocStringType(definition.docStringType());
        assertThat(converter.convert(DocString.create("some doc string"), Object.class),
            is("some_desired_string"));
    }

    public Object convert_doc_string_to_string(String docString) {
        return "some_desired_string";
    }

    @Test
    void must_have_exactly_one_argument() throws NoSuchMethodException {
        Method noArgs = JavaDocStringTypeDefinitionTest.class.getMethod("converts_nothing_to_string");
        assertThrows(InvalidMethodSignatureException.class, () -> new JavaDocStringTypeDefinition("", noArgs, lookup));
        Method twoArgs = JavaDocStringTypeDefinitionTest.class.getMethod("converts_two_strings_to_string", String.class,
            String.class);
        assertThrows(InvalidMethodSignatureException.class, () -> new JavaDocStringTypeDefinition("", twoArgs, lookup));
    }

    public Object converts_nothing_to_string() {
        return "converts_nothing_to_string";
    }

    public Object converts_two_strings_to_string(String arg1, String arg2) {
        return "converts_two_strings_to_string";
    }

    @Test
    void must_have_exactly_string_argument() throws NoSuchMethodException {
        Method method = JavaDocStringTypeDefinitionTest.class.getMethod("converts_object_to_string", Object.class);
        InvalidMethodSignatureException exception = assertThrows(
            InvalidMethodSignatureException.class,
            () -> new JavaDocStringTypeDefinition("", method, lookup));
        assertThat(exception.getMessage(), startsWith("" +
                "A @DocStringType annotated method must have one of these signatures:\n" +
                " * public JsonNode json(String content)\n" +
                "at io.cucumber.java.JavaDocStringTypeDefinitionTest.converts_object_to_string(java.lang.Object)"));
    }

    public Object converts_object_to_string(Object object) {
        return "converts_object_to_string";
    }

    @Test
    void must_return_something() throws NoSuchMethodException {
        Method method = JavaDocStringTypeDefinitionTest.class.getMethod("converts_string_to_void", String.class);
        assertThrows(InvalidMethodSignatureException.class, () -> new JavaDocStringTypeDefinition("", method, lookup));
    }

    public void converts_string_to_void(String docString) {
    }

    @Test
    public void complex_return_types_are_preserved() {
        List<Method> methods = Arrays.stream(JavaDocStringTypeDefinitionTest.class.getMethods())
                .filter(JavaDocStringTypeDefinitionTest::isConvertsToStringMethod)
                .collect(Collectors.toList());

        methods.forEach(method -> {
            JavaDocStringTypeDefinition definition = new JavaDocStringTypeDefinition("text/plain",
                method, lookup);
            registry.defineDocStringType(definition.docStringType());
        });

        TYPES.forEach(type -> {
            if (isMap(type)) {
                assertThat(converter.convert(docString, type), is(Collections.emptyMap()));
            }
            if (isList(type)) {
                assertThat(converter.convert(docString, type), is(Collections.emptyList()));
            }
            if (isOptional(type)) {
                assertThat(converter.convert(docString, type), is(Optional.empty()));
            }
        });
    }

    private static boolean isMap(Type type) {
        return type.getTypeName().startsWith("java.util.Map");
    }

    private static boolean isList(Type type) {
        return type.getTypeName().startsWith("java.util.List");
    }

    private static boolean isOptional(Type type) {
        return type.getTypeName().startsWith("java.util.Optional");
    }

    private static boolean isConvertsToStringMethod(Method method) {
        Type returnType = method.getGenericReturnType();
        return method.getName().startsWith("converts_string_to") &&
                !Void.class.equals(returnType) && !void.class.equals(returnType);
    }

    public Map<String, Map<Integer, Double>> converts_string_to_map_of_string_to_map_of_integer_double(
            String docString
    ) {
        return Collections.emptyMap();
    }

    public Map<String, Map<Integer, Greet>> converts_string_to_map_of_string_to_map_of_integer_greet(String docString) {
        return Collections.emptyMap();
    }

    public Map<Meet, Map<Greet, Leave>> converts_string_to_map_of_meet_to_map_of_greet_leave(String docString) {
        return Collections.emptyMap();
    }

    public Map<String, List<Double>> converts_string_to_map_of_string_to_list_of_double(String docString) {
        return Collections.emptyMap();
    }

    public Map<String, List<Leave>> converts_string_to_map_of_string_to_list_of_leave(String docString) {
        return Collections.emptyMap();
    }

    public List<Map<String, Integer>> converts_string_to_list_of_map_of_string_to_int(String docString) {
        return Collections.emptyList();
    }

    public List<Map<String, Meet>> converts_string_to_list_of_map_of_string_meet(String docString) {
        return Collections.emptyList();
    }

    public List<List<Integer>> converts_string_to_list_of_list_of_int(String docString) {
        return Collections.emptyList();
    }

    public List<List<Greet>> converts_string_to_list_of_list_of_greet(String docString) {
        return Collections.emptyList();
    }

    public Optional<String> converts_string_to_optional_string(String docString) {
        return Optional.empty();
    }

    public Optional<Greet> converts_string_to_optional_greet_type(String docString) {
        return Optional.empty();
    }

    public List<Optional<String>> converts_string_to_list_of_optional_string(String docString) {
        return Collections.emptyList();
    }

    public List<Optional<Leave>> converts_string_to_list_of_optional_leave(String docString) {
        return Collections.emptyList();
    }

    @SuppressWarnings("rawtypes")
    public List<Map> converts_string_to_list_of_map(String docString) {
        return Collections.emptyList();
    }

    @SuppressWarnings("rawtypes")
    public List<List> converts_string_to_list_of_list(String docString) {
        return Collections.emptyList();
    }

    @SuppressWarnings("rawtypes")
    public Map<String, Map> converts_string_to_map_of_string_to_map(String docString) {
        return Collections.emptyMap();
    }

    private static class Greet {
        private final String message;

        Greet(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Greet greet = (Greet) o;
            return Objects.equals(message, greet.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(message);
        }
    }

    private static class Meet {
        private final String message;

        Meet(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Meet meet = (Meet) o;
            return Objects.equals(message, meet.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(message);
        }
    }

    private static class Leave {
        private final String message;

        Leave(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Leave leave = (Leave) o;
            return Objects.equals(message, leave.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(message);
        }
    }

}
