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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaDocStringTypeDefinitionTest {

    @SuppressWarnings("rawtypes")
    private static final Type MAP = new TypeReference<Map>() {
    }.getType();
    private static final Type MAP_OF_STRING_AND_STRING = new TypeReference<Map<String, String>>() {
    }.getType();
    @SuppressWarnings("rawtypes")
    private static final Type MAP_OF_MAP_AND_MAP = new TypeReference<Map<Map, Map>>() {
    }.getType();
    @SuppressWarnings("rawtypes")
    private static final Type LIST = new TypeReference<List>() {
    }.getType();
    private static final Type LIST_OF_STRING = new TypeReference<List<String>>() {
    }.getType();
    @SuppressWarnings("rawtypes")
    private static final Type LIST_OF_LIST = new TypeReference<List<List>>() {
    }.getType();
    @SuppressWarnings("rawtypes")
    private static final Type OPTIONAL = new TypeReference<Optional>() {
    }.getType();
    private static final Type OPTIONAL_STRING = new TypeReference<Optional<String>>() {
    }.getType();
    private static final Type OBJECT = new TypeReference<Object>() {
    }.getType();

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
    public void correct_conversion_is_returned_for_simple_and_complex_return_types() {
        List<String> methodNames = new ArrayList<>();
        methodNames.add("converts_string_to_list_of_string");
        methodNames.add("converts_string_to_list");
        methodNames.add("converts_string_to_list_of_list");
        methodNames.add("converts_string_to_map");
        methodNames.add("converts_string_to_map_of_string_and_string");
        methodNames.add("converts_string_to_map_of_map_and_map");
        methodNames.add("converts_string_to_optional_string");
        methodNames.add("converts_string_to_optional");
        Collections.sort(methodNames);

        methodNames.forEach(methodName -> {
            Method method = null;
            try {
                method = JavaDocStringTypeDefinitionTest.class.getMethod(methodName, String.class);
            } catch (NoSuchMethodException ignored) {
            }
            JavaDocStringTypeDefinition definition = new JavaDocStringTypeDefinition("text/plain",
                method, lookup);
            registry.defineDocStringType(definition.docStringType());
        });

        assertThat(converter.convert(docString, MAP), is(integerMap()));
        assertThat(converter.convert(docString, MAP_OF_STRING_AND_STRING), is(stringMap()));
        assertThat(converter.convert(docString, MAP_OF_MAP_AND_MAP), is(mapOfMaps()));
        assertThat(converter.convert(docString, LIST), is(integerList()));
        assertThat(converter.convert(docString, LIST_OF_STRING), is(stringList()));
        assertThat(converter.convert(docString, LIST_OF_LIST), is(integerListOfList()));
        assertThat(converter.convert(docString, OPTIONAL), is(integerOptional()));
        assertThat(converter.convert(docString, OPTIONAL_STRING), is(stringOptional()));
    }

    private List<String> stringList() {
        List<String> list = new ArrayList<>();
        list.add("Red");
        list.add("Green");
        list.add("Blue");
        return list;
    }

    private List<Integer> integerList() {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        return list;
    }

    private List<List<Integer>> integerListOfList() {
        List<List<Integer>> listOfLists = new ArrayList<>();
        listOfLists.add(integerList());
        return listOfLists;
    }

    private Map<String, String> stringMap() {
        Map<String, String> map = new HashMap<>();
        map.put("R", "Red");
        map.put("G", "Green");
        map.put("B", "Blue");
        return map;
    }

    private Map<Integer, Integer> integerMap() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 1);
        map.put(2, 2);
        map.put(3, 3);
        return map;
    }

    private Map<Map<Integer, Integer>, Map<String, String>> mapOfMaps() {
        Map<Map<Integer, Integer>, Map<String, String>> maps = new HashMap<>();
        maps.put(integerMap(), stringMap());
        return maps;
    }

    private Optional<String> stringOptional() {
        return Optional.of("Red");
    }

    private Optional<Integer> integerOptional() {
        return Optional.of(1);
    }

    public List<String> converts_string_to_list_of_string(String docString) {
        List<String> list = new ArrayList<>();
        list.add("Red");
        list.add("Green");
        list.add("Blue");
        return list;
    }

    @SuppressWarnings("rawtypes")
    public List converts_string_to_list(String docString) {
        List list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        return list;
    }

    @SuppressWarnings("rawtypes")
    public List<List> converts_string_to_list_of_list(String docString) {
        List<List> listOfList = new ArrayList<>();
        List list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        listOfList.add(list);
        return listOfList;
    }

    @SuppressWarnings("rawtypes")
    public Map converts_string_to_map(String docString) {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 1);
        map.put(2, 2);
        map.put(3, 3);
        return map;
    }

    public Map<String, String> converts_string_to_map_of_string_and_string(String docString) {
        Map<String, String> map = new HashMap<>();
        map.put("R", "Red");
        map.put("G", "Green");
        map.put("B", "Blue");
        return map;
    }

    @SuppressWarnings("rawtypes")
    public Map<Map, Map> converts_string_to_map_of_map_and_map(String docString) {
        Map mapOfMapAndMap = new HashMap<>();
        Map mapInteger = new HashMap<>();
        mapInteger.put(1, 1);
        mapInteger.put(2, 2);
        mapInteger.put(3, 3);

        Map mapString = new HashMap<>();
        mapString.put("R", "Red");
        mapString.put("G", "Green");
        mapString.put("B", "Blue");

        mapOfMapAndMap.put(mapInteger, mapString);
        return mapOfMapAndMap;
    }

    public Optional<String> converts_string_to_optional_string(String docString) {
        return Optional.of("Red");
    }

    @SuppressWarnings("rawtypes")
    public Optional converts_string_to_optional(String docString) {
        return Optional.of(1);
    }

}
