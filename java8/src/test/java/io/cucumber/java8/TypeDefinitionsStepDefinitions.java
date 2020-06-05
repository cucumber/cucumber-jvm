package io.cucumber.java8;

import io.cucumber.datatable.DataTable;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class TypeDefinitionsStepDefinitions implements En {

    public TypeDefinitionsStepDefinitions() {
        Given("docstring, defined by lambda",
            (StringBuilder builder) -> assertThat(builder.getClass(), equalTo(StringBuilder.class)));
        DocStringType("doc", (String docString) -> new StringBuilder(docString));

        DataTableType((Map<String, String> entry) -> new Author(entry.get("name"), entry.get("surname"),
            entry.get("famousBook")));

        DataTableType((List<String> row) -> new Book(row.get(0), row.get(1)));

        DataTableType((String cellName) -> new Cell(cellName));

        DataTableType((DataTable dataTable) -> new Literature(dataTable));

        Given("single entry data table, defined by lambda", (Author author) -> {
            assertThat(author.name, equalTo("Fedor"));
            assertThat(author.surname, equalTo("Dostoevsky"));
            assertThat(author.famousBook, equalTo("Crime and Punishment"));
        });

        Given("data table, defined by lambda row transformer", (DataTable dataTable) -> {
            List<Book> books = dataTable.subTable(1, 0).asList(Book.class); // throw
                                                                            // away
                                                                            // table
                                                                            // headers
            Book book1 = new Book("Crime and Punishment", "Raskolnikov");
            Book book2 = new Book("War and Peace", "Bolkonsky");
            assertThat(book1, equalTo(books.get(0)));
            assertThat(book2, equalTo(books.get(1)));
        });

        Given("data table, defined by lambda cell transformer", (DataTable dataTable) -> {
            List<List<Cell>> lists = dataTable.asLists(Cell.class);
            Cell[] actual = lists.stream().flatMap(Collection::stream).toArray(Cell[]::new);
            assertThat(actual[0], equalTo(new Cell("book")));
            assertThat(actual[1], equalTo(new Cell("main character")));
            assertThat(actual[2], equalTo(new Cell("Crime and Punishment")));
            assertThat(actual[3], equalTo(new Cell("Raskolnikov")));
        });

        Given("data table, defined by lambda table transformer", (DataTable dataTable) -> {
            List<String> types = Stream.of("tragedy", "novel").collect(Collectors.toList());
            List<String> characters = Stream.of("Raskolnikov", "Bolkonsky").collect(Collectors.toList());
            Literature expected = new Literature(types, characters);
            Literature actual = dataTable.convert(Literature.class, false);
            assertThat(actual, equalTo(expected));
        });

        Given("data table, defined by lambda", (DataTable dataTable) -> {
            List<Author> authors = dataTable.asList(Author.class);
            Author dostoevsky = new Author("Fedor", "Dostoevsky", "Crime and Punishment");
            Author tolstoy = new Author("Lev", "Tolstoy", "War and Peace");
            assertThat(authors.get(0), equalTo(dostoevsky));
            assertThat(authors.get(1), equalTo(tolstoy));
        });

        // ParameterType with one argument
        Given("{string-builder} parameter, defined by lambda",
            (StringBuilder builder) -> assertThat(builder.toString(), equalTo("string builder")));

        ParameterType("string-builder", ".*", (String str) -> new StringBuilder(str));

        // ParameterType with two String arguments
        Given("balloon coordinates {coordinates}, defined by lambda",
            (Point coordinates) -> assertThat(coordinates.toString(), equalTo("Point[x=123,y=456]")));

        ParameterType("coordinates", "(.+),(.+)", (String x, String y) -> new Point(parseInt(x), parseInt(y)));

        // ParameterType with three arguments
        Given("kebab made from {ingredients}, defined by lambda",
            (StringBuilder ingredients) -> assertThat(ingredients.toString(), equalTo("-mushroom-meat-veg-")));

        ParameterType("ingredients", "(.+), (.+) and (.+)", (String x, String y, String z) -> new StringBuilder()
                .append('-').append(x).append('-').append(y).append('-').append(z).append('-'));

        Given("kebab made from anonymous {}, defined by lambda",
            (StringBuilder coordinates) -> assertThat(coordinates.toString(),
                equalTo("meat-class java.lang.StringBuilder")));

        DefaultParameterTransformer((String fromValue, Type toValueType) -> new StringBuilder().append(fromValue)
                .append('-').append(toValueType));

        Given("default data table cells, defined by lambda", (DataTable dataTable) -> {
            List<List<StringBuilder>> cells = dataTable.asLists(StringBuilder.class);
            assertThat(cells.get(0).get(0).toString(), equalTo("Kebab-class java.lang.StringBuilder"));
        });

        DefaultDataTableCellTransformer(
            (fromValue, toValueType) -> new StringBuilder().append(fromValue).append('-').append(toValueType));

        Given("default data table entries, defined by lambda", (DataTable dataTable) -> {
            List<StringBuilder> cells = dataTable.asList(StringBuilder.class);
            assertThat(cells.get(0).toString(), equalTo("{dinner=Kebab}-class java.lang.StringBuilder"));
        });

        DefaultDataTableEntryTransformer(
            (fromValue, toValueType) -> new StringBuilder().append(fromValue).append('-').append(toValueType));

    }

    public static final class Author {

        private final String name;
        private final String surname;
        private final String famousBook;

        public Author(String name, String surname, String famousBook) {
            this.name = name;
            this.surname = surname;
            this.famousBook = famousBook;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, surname, famousBook);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Author author = (Author) o;
            return Objects.equals(name, author.name) &&
                    Objects.equals(surname, author.surname) &&
                    Objects.equals(famousBook, author.famousBook);
        }

        @Override
        public String toString() {
            return "Author{" +
                    "name='" + name + '\'' +
                    ", surname='" + surname + '\'' +
                    ", famousBook='" + famousBook + '\'' +
                    '}';
        }

    }

    public static final class Point {

        private final int x, y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[x=" + x + ",y=" + y + "]";
        }

    }

    public static final class Book {

        private final String name;
        private final String mainCharacter;

        public Book(String name, String mainCharacter) {
            this.name = name;
            this.mainCharacter = mainCharacter;
        }

        public String getName() {
            return name;
        }

        public String getMainCharacter() {
            return mainCharacter;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Book book = (Book) o;
            return Objects.equals(name, book.name) &&
                    Objects.equals(mainCharacter, book.mainCharacter);
        }

        @Override
        public String toString() {
            return "Book{" +
                    "name='" + name + '\'' +
                    ", mainCharacter='" + mainCharacter + '\'' +
                    '}';
        }

    }

    public static final class Cell {

        private final String name;

        public Cell(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Cell cell = (Cell) o;
            return Objects.equals(name, cell.name);
        }

        @Override
        public String toString() {
            return "Cell{" +
                    "name='" + name + '\'' +
                    '}';
        }

    }

    public static final class Literature {

        private final List<String> types;
        private final List<String> characters;

        public Literature(DataTable dataTable) {
            dataTable = dataTable.subTable(1, 0); // throw away headers
            types = dataTable.transpose().asLists().get(0);
            characters = dataTable.transpose().asLists().get(1);
        }

        public Literature(List<String> types, List<String> characters) {
            this.types = types;
            this.characters = characters;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Literature that = (Literature) o;
            return types.containsAll(that.types) &&
                    characters.containsAll(that.characters);
        }

        @Override
        public String toString() {
            return "Literature{" +
                    "types=" + types +
                    ", characters=" + characters +
                    '}';
        }

    }

}
