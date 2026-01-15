package io.cucumber.datatable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import io.cucumber.datatable.DataTable.TableConverter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;

import static io.cucumber.datatable.DataTable.emptyDataTable;
import static io.cucumber.datatable.TableParser.parse;
import static io.cucumber.datatable.TypeFactory.typeName;
import static java.lang.Double.parseDouble;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Locale.ENGLISH;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@NullMarked
class DataTableTypeRegistryTableConverterTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Type MAP_OF_STRING_TO_COORDINATE = new TypeReference<Map<String, Coordinate>>() {
    }.getType();
    private static final Type MAP_OF_AIR_PORT_CODE_TO_COORDINATE = new TypeReference<Map<AirPortCode, Coordinate>>() {
    }.getType();
    private static final Type MAP_OF_AIR_PORT_CODE_TO_AIR_PORT_CODE = new TypeReference<Map<AirPortCode, AirPortCode>>() {
    }.getType();
    private static final Type MAP_OF_STRING_TO_LIST_OF_DOUBLE = new TypeReference<Map<String, List<Double>>>() {
    }.getType();
    private static final Type MAP_OF_STRING_TO_LIST_OF_DATE = new TypeReference<Map<String, List<Date>>>() {
    }.getType();
    private static final Type LIST_OF_AUTHOR = new TypeReference<List<Author>>() {
    }.getType();
    private static final Type LIST_OF_MAP_OF_STRING_TO_INT = new TypeReference<List<Map<String, Integer>>>() {
    }.getType();
    private static final Type LIST_OF_INT = new TypeReference<List<Integer>>() {
    }.getType();
    private static final Type OPTIONAL_BIG_DECIMAL = new TypeReference<Optional<BigDecimal>>() {
    }.getType();
    private static final Type OPTIONAL_STRING = new TypeReference<Optional<String>>() {
    }.getType();
    private static final Type LIST_OF_OPTIONAL_STRING = new TypeReference<List<Optional<String>>>() {
    }.getType();
    private static final Type OPTIONAL_BIG_INTEGER = new TypeReference<Optional<BigInteger>>() {
    }.getType();
    private static final Type MAP_OF_INT_TO_INT = new TypeReference<Map<Integer, Integer>>() {
    }.getType();
    @SuppressWarnings("rawtypes")
    private static final Type LIST_OF_MAP = new TypeReference<List<Map>>() {
    }.getType();
    @SuppressWarnings("rawtypes")
    private static final Type LIST_OF_LIST = new TypeReference<List<List>>() {
    }.getType();
    private static final Type MAP_OF_INT_TO_STRING = new TypeReference<Map<Integer, String>>() {
    }.getType();
    private static final Type MAP_OF_STRING_TO_MAP_OF_STRING_DOUBLE = new TypeReference<Map<String, Map<String, Double>>>() {
    }.getType();
    private static final Type LIST_OF_MAP_OF_INT_TO_INT = new TypeReference<List<Map<Integer, Integer>>>() {
    }.getType();
    private static final Type LIST_OF_LIST_OF_INT = new TypeReference<List<List<Integer>>>() {
    }.getType();
    private static final Type LIST_OF_LIST_OF_DATE = new TypeReference<List<List<Date>>>() {
    }.getType();
    @SuppressWarnings("rawtypes")
    private static final Type MAP_OF_STRING_TO_MAP = new TypeReference<Map<String, Map>>() {
    }.getType();
    private static final Type MAP_OF_STRING_TO_STRING = new TypeReference<Map<String, String>>() {
    }.getType();
    private static final Type LIST_OF_DOUBLE = new TypeReference<List<Double>>() {
    }.getType();
    private static final Type MAP_OF_STRING_TO_MAP_OF_INTEGER_TO_PIECE = new TypeReference<Map<String, Map<Integer, Piece>>>() {
    }.getType();
    private static final Type OPTIONAL_CHESS_BOARD_TYPE = new TypeReference<Optional<ChessBoard>>() {
    }.getType();
    private static final Type NUMBERED_AUTHOR = new TypeReference<NumberedObject<Author>>() {
    }.getType();
    private static final Type LIST_OF_NUMBERED_AUTHOR = new TypeReference<List<NumberedObject<Author>>>() {
    }.getType();
    private static final TableTransformer<ChessBoard> CHESS_BOARD_TABLE_TRANSFORMER = table -> new ChessBoard(
        table.subTable(1, 1).values());
    private static final TableCellTransformer<Piece> PIECE_TABLE_CELL_TRANSFORMER = Piece::fromString;
    private static final TableCellTransformer<AirPortCode> AIR_PORT_CODE_TABLE_CELL_TRANSFORMER = AirPortCode::fromString;
    private static final TableEntryTransformer<Coordinate> COORDINATE_TABLE_ENTRY_TRANSFORMER = tableEntry -> new Coordinate(
        parseDouble(tableEntry.get("lat")),
        parseDouble(tableEntry.get("lon")));
    private static final TableEntryTransformer<Author> AUTHOR_TABLE_ENTRY_TRANSFORMER = tableEntry -> new Author(
        requireNonNull(tableEntry.get("firstName")),
        requireNonNull(tableEntry.get("lastName")),
        requireNonNull(tableEntry.get("birthDate")));
    private static final TableRowTransformer<Coordinate> COORDINATE_TABLE_ROW_TRANSFORMER = (
            List<@Nullable String> tableRow) -> new Coordinate(
                Double.parseDouble(requireNonNull(tableRow.get(0))),
                Double.parseDouble(requireNonNull(tableRow.get(1))));
    private static final TableEntryTransformer<AirPortCode> AIR_PORT_CODE_TABLE_ENTRY_TRANSFORMER = tableEntry -> new AirPortCode(
        requireNonNull(tableEntry.get("code")));
    private static final TableEntryByTypeTransformer TABLE_ENTRY_BY_TYPE_CONVERTER_SHOULD_NOT_BE_USED = (
            Map<String, String> entry, Type type, TableCellByTypeTransformer cellTransformer) -> {
        throw new IllegalStateException("Should not be used");
    };
    private static final TableCellByTypeTransformer TABLE_CELL_BY_TYPE_CONVERTER_SHOULD_NOT_BE_USED = (value,
            cellType) -> {
        throw new IllegalStateException("Should not be used");
    };
    private static final TableEntryByTypeTransformer JACKSON_TABLE_ENTRY_BY_TYPE_CONVERTER = (entry, type,
            cellTransformer) -> objectMapper.convertValue(entry, objectMapper.constructType(type));
    private static final TableEntryByTypeTransformer JACKSON_NUMBERED_OBJECT_TABLE_ENTRY_CONVERTER = (entry, type,
            cellTransformer) -> {
        if (!(type instanceof ParameterizedType parameterizedType)) {
            throw new IllegalArgumentException("Unsupported type " + type);
        }
        if (!NumberedObject.class.equals(parameterizedType.getRawType())) {
            throw new IllegalArgumentException("Unsupported type " + parameterizedType);
        }
        return convertToNumberedObject(entry, parameterizedType.getActualTypeArguments()[0]);
    };
    private static final TableCellByTypeTransformer JACKSON_TABLE_CELL_BY_TYPE_CONVERTER = (value,
            cellType) -> objectMapper.convertValue(value, objectMapper.constructType(cellType));
    private static final DataTableType DATE_TABLE_CELL_TRANSFORMER = new DataTableType(Date.class,
        (@Nullable String source) -> dateFormat().parse(source));

    private static Object convertToNumberedObject(Map<String, String> numberedEntry, Type type) {
        int number = Integer.parseInt(numberedEntry.get("#"));
        Map<String, String> entry = numberedEntry.entrySet().stream()
                .filter(e -> !"#".equals(e.getKey()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new NumberedObject<>(number, objectMapper.convertValue(entry, objectMapper.constructType(type)));
    }

    private static SimpleDateFormat dateFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat;
    }

    private final DataTableTypeRegistry registry = new DataTableTypeRegistry(ENGLISH);
    private final TableConverter converter = new DataTableTypeRegistryTableConverter(registry);

    @Test
    void convert_to_empty_list__empty_table() {
        DataTable table = emptyDataTable();
        assertEquals(emptyList(), converter.toList(table, Integer.class));
        assertEquals(emptyList(), converter.convert(table, LIST_OF_INT));
    }

    @Test
    void convert_to_empty_lists__empty_table() {
        DataTable table = emptyDataTable();
        assertEquals(emptyList(), converter.toLists(table, Integer.class));
        assertEquals(emptyList(), converter.convert(table, LIST_OF_LIST_OF_INT));
    }

    @Test
    void convert_to_empty_list__only_header() {
        DataTable table = parse("",
            " | firstName | lastName | birthDate |");
        registry.defineDataTableType(new DataTableType(Author.class, AUTHOR_TABLE_ENTRY_TRANSFORMER));
        assertEquals(emptyList(), converter.convert(table, LIST_OF_AUTHOR));
    }

    @Test
    void convert_to_empty_map__blank_first_cell() {
        DataTable table = parse("|   |");
        assertEquals(emptyMap(), converter.toMap(table, Integer.class, Integer.class));
        assertEquals(emptyMap(), converter.convert(table, MAP_OF_INT_TO_INT));
    }

    @Test
    void convert_to_empty_map__empty_table() {
        DataTable table = emptyDataTable();
        assertEquals(emptyMap(), converter.toMap(table, Integer.class, Integer.class));
        assertEquals(emptyMap(), converter.convert(table, MAP_OF_INT_TO_INT));
    }

    @Test
    void convert_to_empty_maps__empty_table() {
        DataTable table = emptyDataTable();
        assertEquals(emptyList(), converter.toMaps(table, Integer.class, Integer.class));
        assertEquals(emptyList(), converter.convert(table, LIST_OF_MAP_OF_INT_TO_INT));
    }

    @Test
    void convert_to_empty_maps__only_header() {
        DataTable table = parse("",
            " | firstName | lastName | birthDate |");
        assertEquals(emptyList(), converter.toMaps(table, String.class, Integer.class));
        assertEquals(emptyList(), converter.convert(table, LIST_OF_MAP_OF_STRING_TO_INT));
    }

    @Test
    void convert_to_empty_table__empty_table() {
        DataTable table = emptyDataTable();
        assertSame(table, converter.convert(table, DataTable.class));
    }

    @Test
    void convert_to_list() {
        DataTable table = parse("",
            "| 3 |",
            "| 5 |",
            "| 6 |",
            "| 7 |");

        List<String> expected = asList("3", "5", "6", "7");

        assertEquals(expected, converter.toList(table, String.class));
        assertEquals(expected, converter.convert(table, List.class));
    }

    @Test
    void convert_to_optional_list() {
        DataTable table = parse("",
            "| 11.22   |",
            "| 255.999 |",
            "|         |");

        List<Optional<BigDecimal>> expected = asList(
            Optional.of(new BigDecimal("11.22")),
            Optional.of(new BigDecimal("255.999")),
            Optional.empty());
        assertEquals(expected, converter.toList(table, OPTIONAL_BIG_DECIMAL));
    }

    @Test
    void convert_to_maps_of_optional() {
        DataTable table = parse("",
            "| header1   | header2   |",
            "| 311       | 12299     |");

        Map<Optional<String>, Optional<BigInteger>> expectedMap = Map.of(
            Optional.of("header1"), Optional.of(new BigInteger("311")),
            Optional.of("header2"), Optional.of(new BigInteger("12299")));
        List<Map<Optional<String>, Optional<BigInteger>>> expected = singletonList(expectedMap);
        assertEquals(expected, converter.toMaps(table, OPTIONAL_STRING, OPTIONAL_BIG_INTEGER));
    }

    @Test
    void convert_to_list__single_column() {
        DataTable table = parse("",
            "| 3 |",
            "| 5 |",
            "| 6 |",
            "| 7 |");

        List<Integer> expected = asList(3, 5, 6, 7);

        assertEquals(expected, converter.toList(table, Integer.class));
        assertEquals(expected, converter.convert(table, LIST_OF_INT));
    }

    @Test
    void convert_to_list__double_column__throws_exception() {
        DataTable table = parse("",
            "| 3 | 5 |",
            "| 6 | 7 |");

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.toList(table, Integer.class));
        assertThat(exception.getMessage(), is(
            """
                    Can't convert DataTable to List<java.lang.Integer>.
                    Please review these problems:

                     - There was a table cell transformer for java.lang.Integer but the table was too wide to use it.
                       Please reduce the table width to use this converter.

                     - There was no table entry or table row transformer registered for java.lang.Integer.
                       Please consider registering a table entry or row transformer.

                     - There was no default table entry transformer registered to transform java.lang.Integer.
                       Please consider registering a default table entry transformer.

                    Note: Usually solving one is enough"""));
    }

    @Test
    void convert_to_list__double_column__single_row__throws_exception() {
        DataTable table = parse("",
            "| 3 | 5 |");

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.toList(table, Integer.class));
        assertThat(exception.getMessage(), is("""
                Can't convert DataTable to List<java.lang.Integer>.
                Please review these problems:

                 - There was a table cell transformer for java.lang.Integer but the table was too wide to use it.
                   Please reduce the table width to use this converter.

                 - There was no table entry or table row transformer registered for java.lang.Integer.
                   Please consider registering a table entry or row transformer.

                Note: Usually solving one is enough"""));
    }

    @Test
    void convert_to_list_of_map() {
        DataTable table = parse("",
            "| firstName   | lastName | birthDate  |",
            "| Annie M. G. | Schmidt  | 1911-03-20 |",
            "| Roald       | Dahl     | 1916-09-13 |",
            "| Astrid      | Lindgren | 1907-11-14 |");

        List<Map<String, String>> expected = asList(
            Map.of(
                "firstName", "Annie M. G.",
                "lastName", "Schmidt",
                "birthDate", "1911-03-20"),
            Map.of(
                "firstName", "Roald",
                "lastName", "Dahl",
                "birthDate", "1916-09-13"),
            Map.of(
                "firstName", "Astrid",
                "lastName", "Lindgren",
                "birthDate", "1907-11-14"));

        assertEquals(expected, converter.convert(table, LIST_OF_MAP));
    }

    @Test
    void convert_to_list_of_object() {
        DataTable table = parse("",
            " | firstName   | lastName | birthDate  |",
            " | Annie M. G. | Schmidt  | 1911-03-20 |",
            " | Roald       | Dahl     | 1916-09-13 |",
            " | Astrid      | Lindgren | 1907-11-14 |");

        List<Author> expected = asList(
            new Author("Annie M. G.", "Schmidt", "1911-03-20"),
            new Author("Roald", "Dahl", "1916-09-13"),
            new Author("Astrid", "Lindgren", "1907-11-14"));
        registry.defineDataTableType(new DataTableType(Author.class, AUTHOR_TABLE_ENTRY_TRANSFORMER));

        assertEquals(expected, converter.toList(table, Author.class));
        assertEquals(expected, converter.convert(table, LIST_OF_AUTHOR));
    }

    @Test
    void convert_to_empty_list_of_object() {
        DataTable table = parse("",
            " | firstName   | lastName | birthDate  |");

        List<Author> expected = emptyList();
        registry.defineDataTableType(new DataTableType(Author.class, AUTHOR_TABLE_ENTRY_TRANSFORMER));

        assertEquals(expected, converter.toList(table, Author.class));
        assertEquals(expected, converter.convert(table, LIST_OF_AUTHOR));
    }

    @Test
    void convert_to_list_of_object__with_default_converters_present() {
        DataTable table = parse("",
            " | firstName   | lastName | birthDate  |",
            " | Annie M. G. | Schmidt  | 1911-03-20 |",
            " | Roald       | Dahl     | 1916-09-13 |",
            " | Astrid      | Lindgren | 1907-11-14 |");

        List<Author> expected = asList(
            new Author("Annie M. G.", "Schmidt", "1911-03-20"),
            new Author("Roald", "Dahl", "1916-09-13"),
            new Author("Astrid", "Lindgren", "1907-11-14"));
        registry.setDefaultDataTableEntryTransformer(TABLE_ENTRY_BY_TYPE_CONVERTER_SHOULD_NOT_BE_USED);
        registry.setDefaultDataTableCellTransformer(TABLE_CELL_BY_TYPE_CONVERTER_SHOULD_NOT_BE_USED);
        registry.defineDataTableType(new DataTableType(Author.class, AUTHOR_TABLE_ENTRY_TRANSFORMER));

        assertEquals(expected, converter.toList(table, Author.class));
        assertEquals(expected, converter.convert(table, LIST_OF_AUTHOR));
    }

    @Test
    void convert_to_list_of_object__using_default_converter() {
        DataTable table = parse("",
            " | firstName   | lastName | birthDate  |",
            " | Annie M. G. | Schmidt  | 1911-03-20 |",
            " | Roald       | Dahl     | 1916-09-13 |",
            " | Astrid      | Lindgren | 1907-11-14 |");

        List<Author> expected = asList(
            new Author("Annie M. G.", "Schmidt", "1911-03-20"),
            new Author("Roald", "Dahl", "1916-09-13"),
            new Author("Astrid", "Lindgren", "1907-11-14"));
        registry.setDefaultDataTableEntryTransformer(JACKSON_TABLE_ENTRY_BY_TYPE_CONVERTER);
        registry.setDefaultDataTableCellTransformer(TABLE_CELL_BY_TYPE_CONVERTER_SHOULD_NOT_BE_USED);

        assertEquals(expected, converter.toList(table, Author.class));
        assertEquals(expected, converter.convert(table, LIST_OF_AUTHOR));
    }

    @Test
    void convert_to_empty_list_of_object__using_default_converter__throws_exception() {
        DataTable table = parse("",
            " | firstName   | lastName | birthDate  |");

        registry.setDefaultDataTableEntryTransformer(JACKSON_TABLE_ENTRY_BY_TYPE_CONVERTER);
        registry.setDefaultDataTableCellTransformer(TABLE_CELL_BY_TYPE_CONVERTER_SHOULD_NOT_BE_USED);

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.convert(table, LIST_OF_AUTHOR));
        assertThat(exception.getMessage(),
            is("""
                    Can't convert DataTable to List<io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Author>.
                    Please review these problems:

                     - There was a default table cell transformer that could be used but the table was too wide to use it.
                       Please reduce the table width to use this converter.

                     - There was no table entry or table row transformer registered for io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Author.
                       Please consider registering a table entry or row transformer.

                     - There was a default table entry transformer that could be used but the table was too short use it.
                       Please increase the table height to use this converter.

                    Note: Usually solving one is enough"""));
    }

    @Test
    void convert_to_list_of_parameterized_object__using_default_converter() {
        DataTable table = parse("",
            "| # | firstName   | lastName | birthDate  |",
            "| 1 | Annie M. G. | Schmidt  | 1911-03-20 |",
            "| 2 | Roald       | Dahl     | 1916-09-13 |",
            "| 3 | Astrid      | Lindgren | 1907-11-14 |");

        registry.setDefaultDataTableEntryTransformer(JACKSON_NUMBERED_OBJECT_TABLE_ENTRY_CONVERTER);
        registry.setDefaultDataTableCellTransformer(TABLE_CELL_BY_TYPE_CONVERTER_SHOULD_NOT_BE_USED);

        List<NumberedObject<Author>> expected = asList(
            new NumberedObject<>(1, new Author("Annie M. G.", "Schmidt", "1911-03-20")),
            new NumberedObject<>(2, new Author("Roald", "Dahl", "1916-09-13")),
            new NumberedObject<>(3, new Author("Astrid", "Lindgren", "1907-11-14")));

        assertEquals(expected, converter.toList(table, NUMBERED_AUTHOR));
        assertEquals(expected, converter.convert(table, LIST_OF_NUMBERED_AUTHOR));
    }

    @Test
    void convert_to_list_of_primitive() {
        DataTable table = parse("",
            "| 3 |",
            "| 5 |",
            "| 6 |",
            "| 7 |");

        List<Integer> expected = asList(3, 5, 6, 7);

        assertEquals(expected, converter.toList(table, Integer.class));
        assertEquals(expected, converter.convert(table, LIST_OF_INT));
    }

    @Test
    void convert_null_cells_to_null() {
        DataTable table = parse("",
            "|   |");

        List<Integer> expected = singletonList(null);

        assertEquals(expected, converter.toList(table, Integer.class));
        assertEquals(expected, converter.convert(table, LIST_OF_INT));
    }

    @Test
    void convert_null_cells_to_empty() {
        DataTable table = parse("",
            "|   |");

        List<Optional<String>> expected = singletonList(Optional.empty());

        assertEquals(expected, converter.toList(table, OPTIONAL_STRING));
        assertEquals(expected, converter.convert(table, LIST_OF_OPTIONAL_STRING));
    }

    @Test
    void convert_to_optional_uses_pre_registered_converter_if_available() {
        DataTable table = DataTable.create(singletonList(singletonList("Hello")));

        List<Optional<String>> expected = singletonList(Optional.of("Goodbye"));

        registry.defineDataTableType(
            new DataTableType(OPTIONAL_STRING, (@Nullable String cell) -> Optional.of("Goodbye")));

        assertEquals(expected, converter.toList(table, OPTIONAL_STRING));
        assertEquals(expected, converter.convert(table, LIST_OF_OPTIONAL_STRING));
    }

    @Test
    void convert_to_list_of_unknown_type__throws_exception__register_transformer() {
        DataTable table = parse("",
            " | firstName   | lastName | birthDate  |",
            " | Annie M. G. | Schmidt  | 1911-03-20 |",
            " | Roald       | Dahl     | 1916-09-13 |",
            " | Astrid      | Lindgren | 1907-11-14 |");

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.convert(table, LIST_OF_AUTHOR));
        assertThat(exception.getMessage(),
            is("""
                    Can't convert DataTable to List<io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Author>.
                    Please review these problems:

                     - There was no table entry or table row transformer registered for io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Author.
                       Please consider registering a table entry or row transformer.

                     - There was no default table entry transformer registered to transform io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Author.
                       Please consider registering a default table entry transformer.

                    Note: Usually solving one is enough"""));
    }

    @Test
    void convert_to_lists() {
        DataTable table = parse("",
            "| 3 | 5 |",
            "| 6 | 7 |");

        List<List<String>> expected = asList(
            asList("3", "5"),
            asList("6", "7"));

        assertEquals(expected, converter.convert(table, LIST_OF_LIST));
        assertEquals(expected, converter.toLists(table, String.class));
    }

    @Test
    void convert_to_lists_of_primitive() {
        DataTable table = parse("",
            "| 3 | 5 |",
            "| 6 | 7 |");

        List<List<Integer>> expected = asList(
            asList(3, 5),
            asList(6, 7));

        assertEquals(expected, converter.toLists(table, Integer.class));
        assertEquals(expected, converter.convert(table, LIST_OF_LIST_OF_INT));
    }

    @Test
    void convert_to_lists_of_unknown_type__throws_exception__register_transformer() {
        DataTable table = parse("",
            " | 1911-03-20 |",
            " | 1916-09-13 |",
            " | 1907-11-14 |");

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.convert(table, LIST_OF_LIST_OF_DATE));

        assertThat(exception.getMessage(), is("""
                Can't convert DataTable to List<List<java.util.Date>>.
                Please review these problems:

                 - There was no table cell transformer registered for java.util.Date.
                   Please consider registering a table cell transformer.

                 - There was no default table cell transformer registered to transform java.util.Date.
                   Please consider registering a default table cell transformer.

                Note: Usually solving one is enough"""));
    }

    @Test
    void convert_to_map() {
        DataTable table = parse("",
            "| 3 | 4 |",
            "| 5 | 6 |");

        Map<String, String> expected = Map.of(
            "3", "4",
            "5", "6");
        assertEquals(expected, converter.toMap(table, String.class, String.class));
        assertEquals(expected, converter.convert(table, Map.class));
    }

    @Test
    void convert_to_map__default_transformers_present() {
        registry.setDefaultDataTableEntryTransformer(TABLE_ENTRY_BY_TYPE_CONVERTER_SHOULD_NOT_BE_USED);
        registry.setDefaultDataTableCellTransformer(TABLE_CELL_BY_TYPE_CONVERTER_SHOULD_NOT_BE_USED);

        DataTable table = parse("",
            "| 3 | 4 |",
            "| 5 | 6 |");

        Map<String, String> expected = Map.of(
            "3", "4",
            "5", "6");

        assertEquals(expected, converter.toMap(table, String.class, String.class));
        assertEquals(expected, converter.convert(table, Map.class));
    }

    @Test
    void convert_to_map__single_column() {
        DataTable table = parse("| 1 |");

        var expected = NullMap.of(
            1, null);

        assertEquals(expected, converter.toMap(table, Integer.class, Integer.class));
        assertEquals(expected, converter.convert(table, MAP_OF_INT_TO_INT));
    }

    @Test
    void convert_to_map_of_object_to_object() {
        DataTable table = parse("",
            "|      | lat       | lon         |",
            "| KMSY | 29.993333 | -90.258056  |",
            "| KSFO | 37.618889 | -122.375    |",
            "| KSEA | 47.448889 | -122.309444 |",
            "| KJFK | 40.639722 | -73.778889  |");

        Map<AirPortCode, Coordinate> expected = Map.of(

            new AirPortCode("KMSY"), new Coordinate(29.993333, -90.258056),
            new AirPortCode("KSFO"), new Coordinate(37.618889, -122.375),
            new AirPortCode("KSEA"), new Coordinate(47.448889, -122.309444),
            new AirPortCode("KJFK"), new Coordinate(40.639722, -73.778889));

        registry.defineDataTableType(new DataTableType(Coordinate.class, COORDINATE_TABLE_ENTRY_TRANSFORMER));
        registry.defineDataTableType(new DataTableType(AirPortCode.class, AIR_PORT_CODE_TABLE_CELL_TRANSFORMER));

        assertEquals(expected, converter.toMap(table, AirPortCode.class, Coordinate.class));
        assertEquals(expected, converter.convert(table, MAP_OF_AIR_PORT_CODE_TO_COORDINATE));
    }

    @Test
    void convert_to_map_of_object_to_object__with_implied_entries_by_count() {
        DataTable table = parse("",
            "| code | lat       | lon         |",
            "| KMSY | 29.993333 | -90.258056  |",
            "| KSFO | 37.618889 | -122.375    |",
            "| KSEA | 47.448889 | -122.309444 |",
            "| KJFK | 40.639722 | -73.778889  |");

        Map<AirPortCode, Coordinate> expected = Map.of(
            new AirPortCode("KMSY"), new Coordinate(29.993333, -90.258056),
            new AirPortCode("KSFO"), new Coordinate(37.618889, -122.375),
            new AirPortCode("KSEA"), new Coordinate(47.448889, -122.309444),
            new AirPortCode("KJFK"), new Coordinate(40.639722, -73.778889));

        registry.defineDataTableType(new DataTableType(Coordinate.class, COORDINATE_TABLE_ENTRY_TRANSFORMER));
        registry.defineDataTableType(new DataTableType(AirPortCode.class, AIR_PORT_CODE_TABLE_ENTRY_TRANSFORMER));

        assertEquals(expected, converter.toMap(table, AirPortCode.class, Coordinate.class));
        assertEquals(expected, converter.convert(table, MAP_OF_AIR_PORT_CODE_TO_COORDINATE));
    }

    @Test
    void convert_to_map_of_object_to_object__default_transformers_present() {
        registry.setDefaultDataTableEntryTransformer(TABLE_ENTRY_BY_TYPE_CONVERTER_SHOULD_NOT_BE_USED);
        registry.setDefaultDataTableCellTransformer(TABLE_CELL_BY_TYPE_CONVERTER_SHOULD_NOT_BE_USED);

        DataTable table = parse("",
            "|      | lat       | lon         |",
            "| KMSY | 29.993333 | -90.258056  |",
            "| KSFO | 37.618889 | -122.375    |",
            "| KSEA | 47.448889 | -122.309444 |",
            "| KJFK | 40.639722 | -73.778889  |");

        Map<AirPortCode, Coordinate> expected = Map.of(
            new AirPortCode("KMSY"), new Coordinate(29.993333, -90.258056),
            new AirPortCode("KSFO"), new Coordinate(37.618889, -122.375),
            new AirPortCode("KSEA"), new Coordinate(47.448889, -122.309444),
            new AirPortCode("KJFK"), new Coordinate(40.639722, -73.778889));

        registry.defineDataTableType(new DataTableType(Coordinate.class, COORDINATE_TABLE_ENTRY_TRANSFORMER));
        registry.defineDataTableType(new DataTableType(AirPortCode.class, AIR_PORT_CODE_TABLE_CELL_TRANSFORMER));

        assertEquals(expected, converter.toMap(table, AirPortCode.class, Coordinate.class));
        assertEquals(expected, converter.convert(table, MAP_OF_AIR_PORT_CODE_TO_COORDINATE));
    }

    @Test
    void convert_to_map_of_object_to_object__using_default_transformers() {
        DataTable table = parse("",
            "|      | lat       | lon         |",
            "| KMSY | 29.993333 | -90.258056  |",
            "| KSFO | 37.618889 | -122.375    |",
            "| KSEA | 47.448889 | -122.309444 |",
            "| KJFK | 40.639722 | -73.778889  |");

        Map<AirPortCode, Coordinate> expected = Map.of(
            new AirPortCode("KMSY"), new Coordinate(29.993333, -90.258056),
            new AirPortCode("KSFO"), new Coordinate(37.618889, -122.375),
            new AirPortCode("KSEA"), new Coordinate(47.448889, -122.309444),
            new AirPortCode("KJFK"), new Coordinate(40.639722, -73.778889));

        registry.setDefaultDataTableEntryTransformer(JACKSON_TABLE_ENTRY_BY_TYPE_CONVERTER);
        registry.setDefaultDataTableCellTransformer(JACKSON_TABLE_CELL_BY_TYPE_CONVERTER);

        assertEquals(expected, converter.toMap(table, AirPortCode.class, Coordinate.class));

        assertEquals(expected, converter.convert(table, MAP_OF_AIR_PORT_CODE_TO_COORDINATE));
    }

    @Test
    void convert_to_map_of_object_to_object__without_implied_entries__using_default_cell_transformer() {
        DataTable table = parse("",
            "| KMSY | KSFO |",
            "| KSFO | KSEA |",
            "| KSEA | KJFK |",
            "| KJFK | AMS  |");

        Map<AirPortCode, AirPortCode> expected = Map.of(
            new AirPortCode("KMSY"), new AirPortCode("KSFO"),
            new AirPortCode("KSFO"), new AirPortCode("KSEA"),
            new AirPortCode("KSEA"), new AirPortCode("KJFK"),
            new AirPortCode("KJFK"), new AirPortCode("AMS"));
        registry.setDefaultDataTableCellTransformer(JACKSON_TABLE_CELL_BY_TYPE_CONVERTER);

        assertEquals(expected, converter.toMap(table, AirPortCode.class, AirPortCode.class));
        assertEquals(expected, converter.convert(table, MAP_OF_AIR_PORT_CODE_TO_AIR_PORT_CODE));
    }

    @Test
    void to_map_of_object_to_object__without_implied_entries__prefers__default_table_entry_converter() {
        DataTable table = parse("",
            "| KMSY | KSFO |",
            "| KSFO | KSEA |",
            "| KSEA | KJFK |",
            "| KJFK | AMS  |");

        Map<AirPortCode, AirPortCode> expected = Map.of(
            new AirPortCode("KMSY"), new AirPortCode("KSFO"),
            new AirPortCode("KSFO"), new AirPortCode("KSEA"),
            new AirPortCode("KSEA"), new AirPortCode("KJFK"),
            new AirPortCode("KJFK"), new AirPortCode("AMS"));

        registry.setDefaultDataTableCellTransformer(JACKSON_TABLE_CELL_BY_TYPE_CONVERTER);

        assertEquals(expected, converter.convert(table, MAP_OF_AIR_PORT_CODE_TO_AIR_PORT_CODE));
    }

    @Test
    void convert_to_map_of_primitive_to_list_of_primitive() {
        DataTable table = parse("",
            "| KMSY | 29.993333 | -90.258056  |",
            "| KSFO | 37.618889 | -122.375    |",
            "| KSEA | 47.448889 | -122.309444 |",
            "| KJFK | 40.639722 | -73.778889  |");

        Map<String, List<Double>> expected = Map.of(
            "KMSY", asList(29.993333, -90.258056),
            "KSFO", asList(37.618889, -122.375),
            "KSEA", asList(47.448889, -122.309444),
            "KJFK", asList(40.639722, -73.778889));

        assertEquals(expected, converter.convert(table, MAP_OF_STRING_TO_LIST_OF_DOUBLE));
    }

    @Test
    void convert_to_map_of_primitive_to_list_of_object() throws ParseException {
        DataTable table = parse("",
            " | Annie M. G. | 1995-03-21 | 1911-03-20 |",
            " | Roald       | 1990-09-13 | 1916-09-13 |",
            " | Astrid      | 1907-10-14 | 1907-11-14 |");

        Map<String, List<Date>> expected = Map.of(
            "Annie M. G.",
            asList(dateFormat().parse("1995-03-21"),
                dateFormat().parse("1911-03-20")),
            "Roald",
            asList(dateFormat().parse("1990-09-13"),
                dateFormat().parse("1916-09-13")),
            "Astrid", asList(dateFormat().parse("1907-10-14"),
                dateFormat().parse("1907-11-14")));

        registry.defineDataTableType(DATE_TABLE_CELL_TRANSFORMER);

        assertEquals(expected, converter.convert(table, MAP_OF_STRING_TO_LIST_OF_DATE));
    }

    @Test
    void convert_to_map_of_primitive_to_list_of_object__with_default_converter() throws ParseException {
        DataTable table = parse("",
            " | Annie M. G. | 1995-03-21 | 1911-03-20 |",
            " | Roald       | 1990-09-13 | 1916-09-13 |",
            " | Astrid      | 1907-10-14 | 1907-11-14 |");

        Map<String, List<Date>> expected = Map.of(
            "Annie M. G.",
            List.of(dateFormat().parse("1995-03-21"),
                dateFormat().parse("1911-03-20")),
            "Roald",
            List.of(dateFormat().parse("1990-09-13"),
                dateFormat().parse("1916-09-13")),
            "Astrid",
            List.of(dateFormat().parse("1907-10-14"),
                dateFormat().parse("1907-11-14")));

        registry.setDefaultDataTableCellTransformer(JACKSON_TABLE_CELL_BY_TYPE_CONVERTER);

        assertEquals(expected, converter.convert(table, MAP_OF_STRING_TO_LIST_OF_DATE));
    }

    @Test
    void convert_to_map_of_primitive_to_list_of_primitive__default_converter_present() {
        registry.setDefaultDataTableEntryTransformer(TABLE_ENTRY_BY_TYPE_CONVERTER_SHOULD_NOT_BE_USED);
        registry.setDefaultDataTableCellTransformer(TABLE_CELL_BY_TYPE_CONVERTER_SHOULD_NOT_BE_USED);

        DataTable table = parse("",
            "| KMSY | 29.993333 | -90.258056  |",
            "| KSFO | 37.618889 | -122.375    |",
            "| KSEA | 47.448889 | -122.309444 |",
            "| KJFK | 40.639722 | -73.778889  |");

        Map<String, List<Double>> expected = Map.of(
            "KMSY", asList(29.993333, -90.258056),
            "KSFO", asList(37.618889, -122.375),
            "KSEA", asList(47.448889, -122.309444),
            "KJFK", asList(40.639722, -73.778889));

        assertEquals(expected, converter.convert(table, MAP_OF_STRING_TO_LIST_OF_DOUBLE));
    }

    @Test
    void convert_to_map_of_primitive_to_map_of_primitive_to_object() {
        DataTable table = parse("",
            "  |   | 1 | 2 | 3 |",
            "  | A | ♘ |   | ♝ |",
            "  | B |   |   |   |",
            "  | C |   | ♝ |   |");

        registry.defineDataTableType(new DataTableType(Piece.class, PIECE_TABLE_CELL_TRANSFORMER));

        var expected = Map.of(
            "A", NullMap.of(
                1, Piece.WHITE_KNIGHT,
                2, null,
                3, Piece.BLACK_BISHOP),

            "B", NullMap.of(
                1, null,
                2, null,
                3, null),
            "C", NullMap.of(
                1, null,
                2, Piece.BLACK_BISHOP,
                3, null));

        assertEquals(expected, converter.convert(table, MAP_OF_STRING_TO_MAP_OF_INTEGER_TO_PIECE));
    }

    @Test
    void convert_to_map_of_primitive_to_map_of_primitive_to_primitive() {
        DataTable table = parse("",
            "|      | lat       | lon         |",
            "| KMSY | 29.993333 | -90.258056  |",
            "| KSFO | 37.618889 | -122.375    |",
            "| KSEA | 47.448889 | -122.309444 |",
            "| KJFK | 40.639722 | -73.778889  |");

        Map<String, Map<String, Double>> expected = Map.of(
            "KMSY", Map.of(

                "lat", 29.993333,
                "lon", -90.258056),
            "KSFO", Map.of(
                "lat", 37.618889,
                "lon", -122.375),
            "KSEA", Map.of(
                "lat", 47.448889,
                "lon", -122.309444),
            "KJFK", Map.of(
                "lat", 40.639722,
                "lon", -73.778889));
        assertEquals(expected, converter.convert(table, MAP_OF_STRING_TO_MAP_OF_STRING_DOUBLE));
    }

    @Test
    void convert_to_map_of_primitive_to_object__blank_first_cell() {
        DataTable table = parse("",
            "|      | lat       | lon         |",
            "| KMSY | 29.993333 | -90.258056  |",
            "| KSFO | 37.618889 | -122.375    |",
            "| KSEA | 47.448889 | -122.309444 |",
            "| KJFK | 40.639722 | -73.778889  |");

        Map<String, Coordinate> expected = Map.of(
            "KMSY", new Coordinate(29.993333, -90.258056),
            "KSFO", new Coordinate(37.618889, -122.375),
            "KSEA", new Coordinate(47.448889, -122.309444),
            "KJFK", new Coordinate(40.639722, -73.778889));

        registry.defineDataTableType(new DataTableType(Coordinate.class, COORDINATE_TABLE_ENTRY_TRANSFORMER));

        assertEquals(expected, converter.toMap(table, String.class, Coordinate.class));
        assertEquals(expected, converter.convert(table, MAP_OF_STRING_TO_COORDINATE));
    }

    @Test
    void convert_to_map_of_primitive_to_primitive() {
        DataTable table = parse("",
            "| 84 | Annie M. G. Schmidt |",
            "| 74 | Roald Dahl          |",
            "| 94 | Astrid Lindgren     |");

        Map<Integer, String> expected = Map.of(
            84, "Annie M. G. Schmidt",
            74, "Roald Dahl",
            94, "Astrid Lindgren");

        assertEquals(expected, converter.toMap(table, Integer.class, String.class));
        assertEquals(expected, converter.convert(table, MAP_OF_INT_TO_STRING));
    }

    @Test
    void convert_to_map_of_string_to_map() {
        DataTable table = parse("",
            "|      | lat       | lon         |",
            "| KMSY | 29.993333 | -90.258056  |",
            "| KSFO | 37.618889 | -122.375    |",
            "| KSEA | 47.448889 | -122.309444 |",
            "| KJFK | 40.639722 | -73.778889  |");

        Map<String, Map<String, String>> expected = Map.of(
            "KMSY", Map.of(
                "lat", "29.993333",
                "lon", "-90.258056"),

            "KSFO", Map.of(
                "lat", "37.618889",
                "lon", "-122.375"),
            "KSEA", Map.of(
                "lat", "47.448889",
                "lon", "-122.309444"),

            "KJFK", Map.of(
                "lat", "40.639722",
                "lon", "-73.778889"));
        assertEquals(expected, converter.convert(table, MAP_OF_STRING_TO_MAP));
    }

    @Test
    void convert_to_map_of_string_to_string__throws_exception__blank_space() {
        DataTable table = parse("",
            "|           | -90.258056  |",
            "| 37.618889 | -122.375    |",
            "| 47.448889 | -122.309444 |",
            "| 40.639722 | -73.778889  |");

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.convert(table, MAP_OF_STRING_TO_LIST_OF_DOUBLE));
        assertThat(exception.getMessage(), is("""
                Can't convert DataTable to Map<%s, %s>.
                There are more values then keys. \
                The first header cell was left blank. \
                You can add a value there"""
                .formatted(typeName(String.class), LIST_OF_DOUBLE)));
    }

    @Test
    void convert_to_map_of_string_to_string__throws_exception__more_then_one_value_per_key() {
        DataTable table = parse("",
            "| KMSY | 29.993333 | -90.258056  |",
            "| KSFO | 37.618889 | -122.375    |",
            "| KSEA | 47.448889 | -122.309444 |",
            "| KJFK | 40.639722 | -73.778889  |");

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.convert(table, MAP_OF_STRING_TO_STRING));
        assertThat(exception.getMessage(), is("""
                Can't convert DataTable to Map<%s, %s>.
                There is more then one value per key. \
                Did you mean to transform to Map<%s, List<%s>> instead?"""
                .formatted(typeName(String.class), typeName(String.class), typeName(String.class),
                    typeName(String.class))));
    }

    @Test
    void convert_to_maps_of_primitive() {
        DataTable table = parse("",
            "| 1 | 2 | 3 |",
            "| 4 | 5 | 6 |",
            "| 7 | 8 | 9 |");

        var expected = asList(
            Map.of(
                1, 4,
                2, 5,
                3, 6),
            Map.of(
                1, 7,
                2, 8,
                3, 9));

        assertEquals(expected, converter.toMaps(table, Integer.class, Integer.class));

        assertEquals(expected, converter.convert(table, LIST_OF_MAP_OF_INT_TO_INT));
    }

    @Test
    void convert_to_maps_of_integer_to_null() {
        DataTable table = parse("",
            "| 1 | 2 |",
            "|   |   |");

        var expected = singletonList(
            NullMap.of(
                1, null,
                2, null));

        assertEquals(expected, converter.toMaps(table, Integer.class, Integer.class));

        assertEquals(expected, converter.convert(table, LIST_OF_MAP_OF_INT_TO_INT));
    }

    @Test
    void convert_to_object() {
        registry.setDefaultDataTableEntryTransformer(TABLE_ENTRY_BY_TYPE_CONVERTER_SHOULD_NOT_BE_USED);
        registry.setDefaultDataTableCellTransformer(TABLE_CELL_BY_TYPE_CONVERTER_SHOULD_NOT_BE_USED);

        DataTable table = parse("",
            "  |   | 1 | 2 | 3 |",
            "  | A | ♘ |   | ♝ |",
            "  | B |   |   |   |",
            "  | C |   | ♝ |   |");

        registry.defineDataTableType(new DataTableType(ChessBoard.class, CHESS_BOARD_TABLE_TRANSFORMER));
        ChessBoard expected = new ChessBoard(asList("♘", "♝", "♝"));

        assertEquals(expected, converter.convert(table, ChessBoard.class));
    }

    @Test
    void convert_to_optional_of_object__must_have_optional_converter() {
        DataTable table = parse("",
            "  |   | 1 | 2 | 3 |",
            "  | A | ♘ |   | ♝ |",
            "  | B |   |   |   |",
            "  | C |   | ♝ |   |");

        registry.defineDataTableType(new DataTableType(ChessBoard.class, CHESS_BOARD_TABLE_TRANSFORMER));

        UndefinedDataTableTypeException exception = assertThrows(
            UndefinedDataTableTypeException.class,
            () -> converter.convert(table, OPTIONAL_CHESS_BOARD_TYPE));
        assertThat(exception.getMessage(),
            is("""
                    Can't convert DataTable to io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$ChessBoard.
                    Please review these problems:

                     - There was no table entry or table row transformer registered for io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$ChessBoard.
                       Please consider registering a table entry or row transformer.

                     - There was no default table entry transformer registered to transform io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$ChessBoard.
                       Please consider registering a default table entry transformer.

                    Note: Usually solving one is enough"""));
    }

    @Test
    void convert_to_empty_optional_object() {
        registry.setDefaultDataTableEntryTransformer(TABLE_ENTRY_BY_TYPE_CONVERTER_SHOULD_NOT_BE_USED);
        registry.setDefaultDataTableCellTransformer(TABLE_CELL_BY_TYPE_CONVERTER_SHOULD_NOT_BE_USED);

        DataTable table = parse("");

        registry.defineDataTableType(new DataTableType(ChessBoard.class, CHESS_BOARD_TABLE_TRANSFORMER));
        assertEquals(Optional.empty(), converter.convert(table, OPTIONAL_CHESS_BOARD_TYPE));
    }

    @Test
    void convert_to_object__more_then_one_item__throws_exception() {
        DataTable table = parse("",
            "| ♘ |",
            "| ♝ |");

        registry.defineDataTableType(new DataTableType(Piece.class, PIECE_TABLE_CELL_TRANSFORMER));

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.convert(table, Piece.class));
        assertThat(exception.getMessage(), is("""
                Can't convert DataTable to %s. \
                The table contained more then one item: [♘, ♝]"""
                .formatted(typeName(Piece.class))));
    }

    @Test
    void convert_to_object__too_wide__throws_exception() {
        DataTable table = parse("",
            "| ♘ | ♝ |");

        registry.defineDataTableType(new DataTableType(Piece.class, PIECE_TABLE_CELL_TRANSFORMER));

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.convert(table, Piece.class));
        assertThat(exception.getMessage(),
            is("""
                    Can't convert DataTable to io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Piece.
                    Please review these problems:

                     - There was a table cell transformer for io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Piece but the table was too wide to use it.
                       Please reduce the table width to use this converter.

                     - There was no table entry or table row transformer registered for io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Piece.
                       Please consider registering a table entry or row transformer.

                    Note: Usually solving one is enough"""));
    }

    @Test
    void convert_to_primitive__empty_table_to_null() {
        DataTable table = emptyDataTable();
        assertNull(converter.convert(table, Integer.class));
    }

    @Test
    void convert_to_primitive__single_cell() {
        DataTable table = parse("| 3 |");
        assertEquals(Integer.valueOf(3), converter.convert(table, Integer.class));
    }

    @Test
    void convert_to_single_object__single_cell() {
        DataTable table = parse("| ♝ |");
        registry.defineDataTableType(new DataTableType(Piece.class, PIECE_TABLE_CELL_TRANSFORMER));

        assertEquals(Piece.BLACK_BISHOP, converter.convert(table, Piece.class));
    }

    @Test
    void convert_to_single_object__single_cell__with_default_transformer_present() {
        registry.setDefaultDataTableEntryTransformer(TABLE_ENTRY_BY_TYPE_CONVERTER_SHOULD_NOT_BE_USED);
        registry.setDefaultDataTableCellTransformer(TABLE_CELL_BY_TYPE_CONVERTER_SHOULD_NOT_BE_USED);

        DataTable table = parse("| ♝ |");
        registry.defineDataTableType(new DataTableType(Piece.class, PIECE_TABLE_CELL_TRANSFORMER));

        assertEquals(Piece.BLACK_BISHOP, converter.convert(table, Piece.class));
    }

    @Test
    void convert_to_single_object__single_cell__using_default_transformer() {
        DataTable table = parse("| BLACK_BISHOP |");
        registry.setDefaultDataTableEntryTransformer(TABLE_ENTRY_BY_TYPE_CONVERTER_SHOULD_NOT_BE_USED);
        registry.setDefaultDataTableCellTransformer(JACKSON_TABLE_CELL_BY_TYPE_CONVERTER);

        assertEquals(Piece.BLACK_BISHOP, converter.convert(table, Piece.class));
    }

    @Test
    void convert_to_parameterized_object__using_default_converter() {
        DataTable table = parse("",
            "| # | firstName   | lastName | birthDate  |",
            "| 1 | Annie M. G. | Schmidt  | 1911-03-20 |");

        registry.setDefaultDataTableEntryTransformer(JACKSON_NUMBERED_OBJECT_TABLE_ENTRY_CONVERTER);
        registry.setDefaultDataTableCellTransformer(TABLE_CELL_BY_TYPE_CONVERTER_SHOULD_NOT_BE_USED);

        NumberedObject<Author> expected = new NumberedObject<>(1, new Author("Annie M. G.", "Schmidt", "1911-03-20"));

        assertEquals(expected, converter.convert(table, NUMBERED_AUTHOR));
    }

    @Test
    void convert_to_table__table_transformer_takes_precedence_over_identity_transform() {
        DataTable table = parse("",
            "  |   | 1 | 2 | 3 |",
            "  | A | ♘ |   | ♝ |",
            "  | B |   |   |   |",
            "  | C |   | ♝ |   |");

        DataTable expected = emptyDataTable();
        registry.defineDataTableType(new DataTableType(DataTable.class, (DataTable raw) -> expected));

        assertSame(expected, converter.convert(table, DataTable.class));
    }

    @Test
    void convert_to_table__transposed() {
        DataTable table = parse("",
            "  |   | 1 | 2 | 3 |",
            "  | A | ♘ |   | ♝ |",
            "  | B |   |   |   |",
            "  | C |   | ♝ |   |");

        assertEquals(table.transpose(), converter.convert(table, DataTable.class, true));
    }

    @Test
    void convert_to_unknown_type__throws_exception() {
        DataTable table = parse("",
            "| ♘ |");

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.convert(table, Piece.class));
        assertThat(exception.getMessage(),
            is("""
                    Can't convert DataTable to io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Piece.
                    Please review these problems:

                     - There was no table entry or table row transformer registered for io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Piece.
                       Please consider registering a table entry or row transformer.

                     - There was no table cell transformer registered for io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Piece.
                       Please consider registering a table cell transformer.

                     - There was no default table cell transformer registered to transform io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Piece.
                       Please consider registering a default table cell transformer.

                    Note: Usually solving one is enough"""));
    }

    @Test
    void convert_to_unknown_type__throws_exception__with_table_entry_converter_present__throws_exception() {
        DataTable table = parse("",
            "| ♘ |");
        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.convert(table, Piece.class));
        assertThat(exception.getMessage(),
            is("""
                    Can't convert DataTable to io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Piece.
                    Please review these problems:

                     - There was no table entry or table row transformer registered for io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Piece.
                       Please consider registering a table entry or row transformer.

                     - There was no table cell transformer registered for io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Piece.
                       Please consider registering a table cell transformer.

                     - There was no default table cell transformer registered to transform io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Piece.
                       Please consider registering a default table cell transformer.

                    Note: Usually solving one is enough"""));
    }

    @Test
    void to_list__single_column__throws_exception__register_transformer() {
        DataTable table = parse("",
            "| ♘ |",
            "| ♝ |");

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.toList(table, Piece.class));
        assertThat(exception.getMessage(),
            is("""
                    Can't convert DataTable to List<io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Piece>.
                    Please review these problems:

                     - There was no table entry or table row transformer registered for io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Piece.
                       Please consider registering a table entry or row transformer.

                     - There was no table cell transformer registered for io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Piece.
                       Please consider registering a table cell transformer.

                     - There was no default table entry transformer registered to transform io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Piece.
                       Please consider registering a default table entry transformer.

                     - There was no default table cell transformer registered to transform io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Piece.
                       Please consider registering a default table cell transformer.

                    Note: Usually solving one is enough"""));
    }

    @Test
    void to_list_of_unknown_type__throws_exception() {
        DataTable table = parse("",
            " | firstName   | lastName | birthDate  |",
            " | Annie M. G. | Schmidt  | 1911-03-20 |",
            " | Roald       | Dahl     | 1916-09-13 |",
            " | Astrid      | Lindgren | 1907-11-14 |");

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.toList(table, Author.class));
        assertThat(exception.getMessage(),
            is("""
                    Can't convert DataTable to List<io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Author>.
                    Please review these problems:

                     - There was no table entry or table row transformer registered for io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Author.
                       Please consider registering a table entry or row transformer.

                     - There was no default table entry transformer registered to transform io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Author.
                       Please consider registering a default table entry transformer.

                    Note: Usually solving one is enough"""));
    }

    @Test
    void to_lists_of_unknown_type__throws_exception() {
        DataTable table = parse("",
            " | firstName   | lastName | birthDate  |",
            " | Annie M. G. | Schmidt  | 1911-03-20 |",
            " | Roald       | Dahl     | 1916-09-13 |",
            " | Astrid      | Lindgren | 1907-11-14 |");

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class, () -> converter.toLists(table, Author.class));
        assertThat(exception.getMessage(),
            is("""
                    Can't convert DataTable to List<List<io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Author>>.
                    Please review these problems:

                     - There was no table cell transformer registered for io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Author.
                       Please consider registering a table cell transformer.

                     - There was no default table cell transformer registered to transform io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Author.
                       Please consider registering a default table cell transformer.

                    Note: Usually solving one is enough"""));
    }

    @Test
    void to_map__duplicate_keys__throws_exception() {
        DataTable table = parse("",
            "|      | lat       | lon         |",
            "| KMSY | 29.993333 | -90.258056  |",
            "| KSEA | 47.448889 | -122.309444 |",
            "| KSFO | 37.618889 | -122.375    |",
            "| KSEA | 47.448889 | -122.309444 |",
            "| KJFK | 40.639722 | -73.778889  |");

        registry.defineDataTableType(new DataTableType(AirPortCode.class, AIR_PORT_CODE_TABLE_CELL_TRANSFORMER));
        registry.defineDataTableType(new DataTableType(Coordinate.class, COORDINATE_TABLE_ENTRY_TRANSFORMER));

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.toMap(table, AirPortCode.class, Coordinate.class));
        assertThat(exception.getMessage(), startsWith("""
                Can't convert DataTable to Map<%s, %s>.
                Encountered duplicate key"""
                .formatted(typeName(AirPortCode.class), typeName(Coordinate.class))));
    }

    @Test
    void to_map_of_entry_to_primitive__blank_first_cell__throws_exception__key_type_was_entry() {
        DataTable table = parse("",
            "| code |                                                   |",
            "| KMSY | Louis Armstrong New Orleans International Airport |",
            "| KSFO | San Francisco International Airport               |",
            "| KSEA | Seattle–Tacoma International Airport              |",
            "| KJFK | John F. Kennedy International Airport             |");

        registry.defineDataTableType(new DataTableType(AirPortCode.class, AIR_PORT_CODE_TABLE_ENTRY_TRANSFORMER));

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.toMap(table, AirPortCode.class, String.class));
        assertThat(exception.getMessage(),
            is("""
                    Can't convert DataTable to Map<io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$AirPortCode, java.lang.String>.
                    The first cell was either blank or you have registered a TableEntryTransformer for the key type.

                    This requires that there is a TableEntryTransformer for the value type but I couldn't find any.

                    You can either:

                      1) Use a DataTableType that uses a TableEntryTransformer for class java.lang.String

                      2) Add a key to the first cell and use a DataTableType that uses a TableEntryTransformer for class io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$AirPortCode"""));
    }

    @Test
    void to_map_of_entry_to_row__throws_exception__more_values_then_keys() {
        DataTable table = parse("",
            "| code | 29.993333 | -90.258056  |",
            "| KSFO | 37.618889 | -122.375    |",
            "| KSEA | 47.448889 | -122.309444 |",
            "| KJFK | 40.639722 | -73.778889  |");

        registry.defineDataTableType(new DataTableType(AirPortCode.class, AIR_PORT_CODE_TABLE_ENTRY_TRANSFORMER));
        registry.defineDataTableType(new DataTableType(Coordinate.class, COORDINATE_TABLE_ROW_TRANSFORMER));

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.toMap(table, AirPortCode.class, Coordinate.class));
        assertThat(exception.getMessage(), is("""
                Can't convert DataTable to Map<%s, %s>.
                There are more values then keys. \
                Did you use a TableEntryTransformer for the key \
                while using a TableRow or TableCellTransformer for the value?"""
                .formatted(typeName(AirPortCode.class), typeName(Coordinate.class))));
    }

    @Test
    void to_map_of_object_to_unknown_type__throws_exception__register_table_entry_transformer() {
        DataTable table = parse("",
            "| code | lat       | lon         |",
            "| KMSY | 29.993333 | -90.258056  |",
            "| KSFO | 37.618889 | -122.375    |",
            "| KSEA | 47.448889 | -122.309444 |",
            "| KJFK | 40.639722 | -73.778889  |");

        registry.defineDataTableType(new DataTableType(AirPortCode.class, AIR_PORT_CODE_TABLE_ENTRY_TRANSFORMER));

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.toMap(table, AirPortCode.class, Coordinate.class));
        assertThat(exception.getMessage(),
            startsWith("""
                    Can't convert DataTable to Map<%s, %s>.
                    The first cell was either blank or you have registered a TableEntryTransformer for the key type."""
                    .formatted(typeName(AirPortCode.class), typeName(Coordinate.class))));
    }

    @Test
    void to_map_of_primitive_to_entry__throws_exception__more_keys_then_values() {
        DataTable table = parse("",
            "| code | lat       | lon         |",
            "| KMSY | 29.993333 | -90.258056  |",
            "| KSFO | 37.618889 | -122.375    |",
            "| KSEA | 47.448889 | -122.309444 |",
            "| KJFK | 40.639722 | -73.778889  |");

        registry.defineDataTableType(new DataTableType(Coordinate.class, COORDINATE_TABLE_ENTRY_TRANSFORMER));

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.toMap(table, String.class, Coordinate.class));
        assertThat(exception.getMessage(), is("""
                Can't convert DataTable to Map<%s, %s>.
                There are more keys than values. \
                Did you use a TableEntryTransformer for the value \
                while using a TableRow or TableCellTransformer for the keys?"""
                .formatted(typeName(String.class), typeName(Coordinate.class))));
    }

    @Test
    void to_map_of_primitive_to_primitive__blank_first_cell__throws_exception__first_cell_was_blank() {
        DataTable table = parse("",
            " |                     | birthDate  |",
            " | Annie M. G. Schmidt | 1911-03-20 |",
            " | Roald Dahl          | 1916-09-13 |",
            " | Astrid Lindgren     | 1907-11-14 |");

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.toMap(table, String.class, String.class));
        assertThat(exception.getMessage(),
            startsWith("""
                    Can't convert DataTable to Map<%s, %s>.
                    The first cell was either blank or you have registered a TableEntryTransformer for the key type."""
                    .formatted(typeName(String.class), typeName(String.class))));
    }

    @Test
    void to_map_of_unknown_key_type__throws_exception() {
        DataTable table = parse("",
            " | name                | birthDate  |",
            " | Annie M. G. Schmidt | 1911-03-20 |",
            " | Roald Dahl          | 1916-09-13 |",
            " | Astrid Lindgren     | 1907-11-14 |");

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.toMap(table, Author.class, String.class));
        assertThat(exception.getMessage(),
            is("""
                    Can't convert DataTable to Map<io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Author, java.lang.String>.
                    Please review these problems:

                     - There was no table entry or table row transformer registered for io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Author.
                       Please consider registering a table entry or row transformer.

                     - There was no table cell transformer registered for io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Author.
                       Please consider registering a table cell transformer.

                     - There was no default table entry transformer registered to transform io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Author.
                       Please consider registering a default table entry transformer.

                     - There was no default table cell transformer registered to transform io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Author.
                       Please consider registering a default table cell transformer.

                    Note: Usually solving one is enough"""));
    }

    @Test
    void to_map_of_unknown_type_to_object__throws_exception__register_table_cell_transformer() {
        DataTable table = parse("",
            "|      | lat       | lon         |",
            "| KMSY | 29.993333 | -90.258056  |",
            "| KSFO | 37.618889 | -122.375    |",
            "| KSEA | 47.448889 | -122.309444 |",
            "| KJFK | 40.639722 | -73.778889  |");

        registry.defineDataTableType(new DataTableType(Coordinate.class, COORDINATE_TABLE_ENTRY_TRANSFORMER));

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.toMap(table, AirPortCode.class, Coordinate.class));
        assertThat(exception.getMessage(),
            is("""
                    Can't convert DataTable to Map<io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$AirPortCode, io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Coordinate>.
                    Please review these problems:

                     - There was no table cell transformer registered for io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$AirPortCode.
                       Please consider registering a table cell transformer.

                     - There was no default table cell transformer registered to transform io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$AirPortCode.
                       Please consider registering a default table cell transformer.

                    Note: Usually solving one is enough"""));
    }

    @Test
    void to_map_of_unknown_value_type__throws_exception() {
        DataTable table = parse("",
            " | Annie M. G. Schmidt | 1911-03-20 |",
            " | Roald Dahl          | 1916-09-13 |",
            " | Astrid Lindgren     | 1907-11-14 |");
        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.toMap(table, String.class, Date.class));
        assertThat(exception.getMessage(), is("""
                Can't convert DataTable to Map<java.lang.String, java.util.Date>.
                Please review these problems:

                 - There was no table entry transformer registered for java.util.Date.
                   Please consider registering a table entry transformer.

                 - There was no table cell transformer registered for java.util.Date.
                   Please consider registering a table cell transformer.

                 - There was no default table cell transformer registered to transform java.util.Date.
                   Please consider registering a default table cell transformer.

                Note: Usually solving one is enough"""));
    }

    @Test
    void to_map_of_primitive_to_list_of_unknown__throws_exception() {
        DataTable table = parse("",
            " | Annie M. G. | 1995-03-21 | 1911-03-20 |",
            " | Roald       | 1990-09-13 | 1916-09-13 |",
            " | Astrid      | 1907-10-14 | 1907-11-14 |");

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.convert(table, MAP_OF_STRING_TO_LIST_OF_DATE));
        assertThat(exception.getMessage(), is("""
                Can't convert DataTable to Map<java.lang.String, java.util.List<java.util.Date>>.
                Please review these problems:

                 - There was no table cell transformer registered for java.util.Date.
                   Please consider registering a table cell transformer.

                 - There was no default table cell transformer registered to transform java.util.Date.
                   Please consider registering a default table cell transformer.

                Note: Usually solving one is enough"""));
    }

    @Test
    void to_maps_cant_convert_table_with_duplicate_keys() {
        DataTable table = parse("",
            "| 1 | 1 | 1 |",
            "| 4 | 5 | 6 |",
            "| 7 | 8 | 9 |");

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.toMaps(table, Integer.class, Integer.class));
        assertThat(exception.getMessage(), is(("Can't convert DataTable to Map<%s, %s>.\n" +
                "Encountered duplicate key 1 with values 4 and 5")
                .formatted(typeName(Integer.class), typeName(Integer.class))));
    }

    @Test
    void to_maps_cant_convert_table_with_duplicate_null_keys() {
        DataTable table = parse("",
            "|   |   |",
            "| 1 | 2 |");

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.toMaps(table, Integer.class, Integer.class));
        assertThat(exception.getMessage(), is(("Can't convert DataTable to Map<%s, %s>.\n" +
                "Encountered duplicate key null with values 1 and 2")
                .formatted(typeName(Integer.class), typeName(Integer.class))));
    }

    @Test
    void to_maps_of_unknown_key_type__throws_exception__register_table_cell_transformer() {
        DataTable table = parse("",
            "| lat       | lon         |",
            "| 29.993333 | -90.258056  |",
            "| 37.618889 | -122.375    |",
            "| 47.448889 | -122.309444 |",
            "| 40.639722 | -73.778889  |");

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.toMaps(table, String.class, Coordinate.class));
        assertThat(exception.getMessage(),
            is("""
                    Can't convert DataTable to List<Map<java.lang.String, io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Coordinate>>.
                    Please review these problems:

                     - There was no table cell transformer registered for io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Coordinate.
                       Please consider registering a table cell transformer.

                    Note: Usually solving one is enough"""));
    }

    @Test
    void to_maps_of_unknown_value_type__throws_exception__register_table_cell_transformer() {
        DataTable table = parse("",
            "| ♙  | ♟  |",
            "| a2 | a7 |",
            "| b2 | b7 |",
            "| c2 | c7 |",
            "| d2 | d7 |",
            "| e2 | e7 |",
            "| f2 | f7 |",
            "| g2 | g7 |",
            "| h2 | h7 |");

        CucumberDataTableException exception = assertThrows(
            CucumberDataTableException.class,
            () -> converter.toMaps(table, Piece.class, String.class));
        assertThat(exception.getMessage(),
            is("""
                    Can't convert DataTable to List<Map<io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Piece, java.lang.String>>.
                    Please review these problems:

                     - There was no table cell transformer registered for io.cucumber.datatable.DataTableTypeRegistryTableConverterTest$Piece.
                       Please consider registering a table cell transformer.

                    Note: Usually solving one is enough"""));
    }

    private record NumberedObject<T>(int number, T value) {

        @Override
        public String toString() {
            return "%d: %s".formatted(number, value);
        }
    }

    private enum Piece {
        BLACK_PAWN("♟"),
        BLACK_BISHOP("♝"),
        WHITE_PAWN("♙"),
        WHITE_KNIGHT("♘");

        private final String glyp;

        Piece(String glyp) {
            this.glyp = glyp;
        }

        static @Nullable Piece fromString(@Nullable String glyp) {
            for (Piece piece : values()) {
                if (piece.glyp.equals(glyp)) {
                    return piece;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return glyp;
        }
    }

    public static final class AirPortCode {
        private final String code;

        AirPortCode(String code) {
            this.code = code;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            AirPortCode that = (AirPortCode) o;

            return code.equals(that.code);
        }

        @Override
        public int hashCode() {
            return code.hashCode();
        }

        @Override
        public String toString() {
            return "AirPortCode{" +
                    "code='" + code + '\'' +
                    '}';
        }

        @JsonCreator
        static @Nullable AirPortCode fromString(@Nullable String code) {
            if (code == null) {
                return null;
            }

            return new AirPortCode(code);
        }
    }

    @SuppressWarnings({ "unused", "RedundantModifier" })
    public static final class Author {

        private @Nullable String firstName;
        private @Nullable String lastName;
        private @Nullable String birthDate;

        public Author(String firstName, String lastName, String birthDate) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.birthDate = birthDate;
        }

        public Author() {
            // default constructor
        }

        public void setFirstName(@Nullable String firstName) {
            this.firstName = firstName;
        }

        public void setLastName(@Nullable String lastName) {
            this.lastName = lastName;
        }

        public void setBirthDate(@Nullable String birthDate) {
            this.birthDate = birthDate;
        }

        public @Nullable String getFirstName() {
            return firstName;
        }

        public @Nullable String getLastName() {
            return lastName;
        }

        public @Nullable String getBirthDate() {
            return birthDate;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Author author))
                return false;
            return Objects.equals(firstName, author.firstName) && Objects.equals(lastName, author.lastName)
                    && Objects.equals(birthDate, author.birthDate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(firstName, lastName, birthDate);
        }

        @Override
        public String toString() {
            return "Author{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", birthDate='" + birthDate + '\'' +
                    '}';
        }
    }

    @SuppressWarnings({ "unused", "RedundantModifier" })
    public static final class Coordinate {

        double lat;
        double lon;

        Coordinate() {
        }

        public double getLat() {
            return lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }

        private Coordinate(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Coordinate that))
                return false;
            return Double.compare(lat, that.lat) == 0 && Double.compare(lon, that.lon) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(lat, lon);
        }
    }

    private static final class ChessBoard {
        private final Multiset<Piece> pieces = HashMultiset.create();

        ChessBoard(List<String> glyphs) {
            for (String glyph : glyphs) {
                Piece piece = Piece.fromString(glyph);
                if (piece != null) {
                    pieces.add(piece);
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            ChessBoard that = (ChessBoard) o;

            return pieces.equals(that.pieces);
        }

        @Override
        public int hashCode() {
            return pieces.hashCode();
        }

        @Override
        public String toString() {
            return pieces.toString();
        }
    }

}
