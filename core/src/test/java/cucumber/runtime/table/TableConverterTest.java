package cucumber.runtime.table;

import cucumber.api.DataTable;
import cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter;
import cucumber.deps.com.thoughtworks.xstream.converters.SingleValueConverter;
import cucumber.deps.com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import cucumber.runtime.CucumberException;
import cucumber.runtime.ParameterInfo;
import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TableConverterTest {

    private static final String YYYY_MM_DD = "yyyy-MM-dd";
    private static final ParameterInfo PARAMETER_INFO = new ParameterInfo(null, YYYY_MM_DD, null, null);

    @Test
    public void converts_table_of_single_column_to_list_of_integers() {
        DataTable table = TableParser.parse("|3|\n|5|\n|6|\n|7|\n", null);
        assertEquals(asList(3, 5, 6, 7), table.asList(Integer.class));
    }

    @Test
    public void converts_table_of_two_columns_to_map() {
        DataTable table = TableParser.parse("|3|c|\n|5|e|\n|6|f|\n", null);
        Map<Integer, String> expected = new HashMap<Integer, String>() {{
            put(3, "c");
            put(5, "e");
            put(6, "f");
        }};

        assertEquals(expected, table.asMap(Integer.class, String.class));
    }

    public static class WithoutStringConstructor {
        public String count;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            WithoutStringConstructor thingie = (WithoutStringConstructor) o;

            if (!count.equals(thingie.count)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return count.hashCode();
        }

        @Override
        public String toString() {
            return "Thingie{" +
                    "count=" + count +
                    '}';
        }

        public WithoutStringConstructor val(String s) {
            count = s;
            return this;
        }
    }

    @Test
    public void converts_table_of_single_column_to_list_of_without_string_constructor() {
        DataTable table = TableParser.parse("|count|\n|5|\n|6|\n|7|\n", null);
        List<WithoutStringConstructor> expected = asList(new WithoutStringConstructor().val("5"), new WithoutStringConstructor().val("6"), new WithoutStringConstructor().val("7"));
        assertEquals(expected, table.asList(WithoutStringConstructor.class));
    }

    public static class WithStringConstructor extends WithoutStringConstructor {
        public WithStringConstructor(String anything) {
            count = anything;
        }
    }

    @Test
    public void converts_table_of_single_column_to_list_of_with_string_constructor() {
        DataTable table = TableParser.parse("|count|\n|5|\n|6|\n|7|\n", null);
        List<WithStringConstructor> expected = asList(new WithStringConstructor("count"), new WithStringConstructor("5"), new WithStringConstructor("6"), new WithStringConstructor("7"));
        assertEquals(expected, table.asList(WithStringConstructor.class));
    }

    @Test
    public void converts_table_of_several_columns_to_list_of_integers() {
        DataTable table = TableParser.parse("|3|5|\n|6|7|\n", null);
        List<Integer> converted = table.asList(Integer.class);
        assertEquals(asList(3, 5, 6, 7), converted);
    }

    @Test
    public void converts_table_to_list_of_list_of_integers_and_back() {
        DataTable table = TableParser.parse("|3|5|\n|6|7|\n", null);
        List<List<Integer>> converted = table.asLists(Integer.class);
        assertEquals(asList(asList(3, 5), asList(6, 7)), converted);
        assertEquals("      | 3 | 5 |\n      | 6 | 7 |\n", table.toTable(converted).toString());
    }

    public static enum Color {
        RED, GREEN, BLUE
    }

    @Test
    public void converts_table_of_single_column_to_enums() {
        DataTable table = TableParser.parse("|RED|\n|GREEN|\n", null);
        assertEquals(asList(Color.RED, Color.GREEN), table.asList(Color.class));
    }

    @Test
    public void converts_table_of_single_column_to_nullable_enums() {
        DataTable table = TableParser.parse("|RED|\n||\n", null);
        assertEquals(asList(Color.RED, null), table.asList(Color.class));
    }

    @Test
    public void converts_to_map_of_enum_to_int() {
        DataTable table = TableParser.parse("|RED|BLUE|\n|6|7|\n|8|9|\n", null);
        HashMap<Color, Integer> map1 = new HashMap<Color, Integer>() {{
            put(Color.RED, 6);
            put(Color.BLUE, 7);
        }};
        HashMap<Color, Integer> map2 = new HashMap<Color, Integer>() {{
            put(Color.RED, 8);
            put(Color.BLUE, 9);
        }};
        List<Map<Color, Integer>> converted = table.asMaps(Color.class, Integer.class);
        assertEquals(asList(map1, map2), converted);
    }

    public static class UserPojo {
        private Date birthDate;
        private Calendar deathCal;
    }

    @Test
    public void converts_table_to_list_of_pojo_and_almost_back() {
        DataTable table = TableParser.parse("|Birth Date|Death Cal|\n|1957-05-10|1979-02-02|\n", PARAMETER_INFO);
        List<UserPojo> converted = table.asList(UserPojo.class);
        assertEquals(sidsBirthday(), converted.get(0).birthDate);
        assertEquals(sidsDeathcal(), converted.get(0).deathCal);
        assertEquals("      | birthDate  | deathCal   |\n      | 1957-05-10 | 1979-02-02 |\n", table.toTable(converted).toString());
    }

    @XStreamConverter(JavaBeanConverter.class)
    public static class UserBean {
        private Date birthDateX;
        private Calendar deathCalX;

        public Date getBirthDate() {
            return this.birthDateX;
        }

        public void setBirthDate(Date birthDate) {
            this.birthDateX = birthDate;
        }

        public Calendar getDeathCal() {
            return deathCalX;
        }

        public void setDeathCal(Calendar deathCal) {
            this.deathCalX = deathCal;
        }
    }

    @Test
    public void converts_to_list_of_java_bean_and_almost_back() {
        DataTable table = TableParser.parse("|Birth Date|Death Cal|\n|1957-05-10|1979-02-02|\n", PARAMETER_INFO);
        List<UserBean> converted = table.asList(UserBean.class);
        assertEquals(sidsBirthday(), converted.get(0).getBirthDate());
        assertEquals(sidsDeathcal(), converted.get(0).getDeathCal());
        assertEquals("      | birthDate  | deathCal   |\n      | 1957-05-10 | 1979-02-02 |\n", table.toTable(converted).toString());
    }

    public static class BlogBean {
        private String author;
        private List<String> tags;
        private String post;

        public String getPost() {
            return post;
        }

        public void setPost(String post) {
            this.post = post;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }
    }

    @Test
    public void throws_cucumber_exception_for_complex_types() {
        BlogBean blog = new BlogBean();
        blog.setAuthor("Tom Scott");
        blog.setTags(asList("Language", "Linguistics", " Mycenaean Greek"));
        blog.setPost("Linear B is a syllabic script that was used for writing Mycenaean Greek...");
        try {
            DataTable.create(Collections.singletonList(blog));
            fail();
        } catch (CucumberException expected) {
            assertEquals("" +
                    "Don't know how to convert \"cucumber.runtime.table.TableConverterTest$BlogBean.tags\" into a table entry.\n" +
                    "Either exclude tags from the table by selecting the fields to include:\n" +
                    "\n" +
                    "DataTable.create(entries, \"Field\", \"Other Field\")\n" +
                    "\n" +
                    "Or try writing your own converter:\n" +
                    "\n" +
                    "@cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter(TagsConverter.class)\n" +
                    "private List tags;\n",
                expected.getMessage());
        }
    }

    @Test
    public void converts_empty_complex_types_and_almost_back() {
        DataTable table = TableParser.parse("" +
            "|Author   |Tags |Post            |\n" +
            "|Tom Scott|     |Linear B is a...|\n", PARAMETER_INFO);
        List<BlogBean> converted = table.asList(BlogBean.class);
        BlogBean blog = converted.get(0);
        assertEquals("Tom Scott", blog.getAuthor());
        assertEquals(emptyList(), blog.getTags());
        assertEquals("Linear B is a...", blog.getPost());
        assertEquals("" +
                "      | author    | tags | post             |\n" +
                "      | Tom Scott |      | Linear B is a... |\n",
            table.toTable(converted).toString());
    }

    public static class AnnotatedBlogBean {
        private String author;
        @XStreamConverter(TagsConverter.class)
        private List<String> tags;
        private String post;

        public String getPost() {
            return post;
        }

        public void setPost(String post) {
            this.post = post;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }
    }

    public static class TagsConverter implements SingleValueConverter {

        @Override
        public String toString(Object o) {
            return o.toString().replace("[", "").replace("]", "");
        }

        @Override
        public Object fromString(String s) {
            return asList(s.split(", "));
        }

        @Override
        public boolean canConvert(Class type) {
            return List.class.isAssignableFrom(type);
        }
    }

    @Test
    public void converts_annotated_complex_types_and_almost_back() {
        DataTable table = TableParser.parse("" +
            "|Author   |Tags                                  |Post            |\n" +
            "|Tom Scott|Language, Linguistics, Mycenaean Greek|Linear B is a...|\n", PARAMETER_INFO);
        List<AnnotatedBlogBean> converted = table.asList(AnnotatedBlogBean.class);
        AnnotatedBlogBean blog = converted.get(0);
        assertEquals("Tom Scott", blog.getAuthor());
        assertEquals(asList("Language", "Linguistics", "Mycenaean Greek"), blog.getTags());
        assertEquals("Linear B is a...", blog.getPost());
        assertEquals("" +
                "      | author    | tags                                   | post             |\n" +
                "      | Tom Scott | Language, Linguistics, Mycenaean Greek | Linear B is a... |\n",
            table.toTable(converted).toString());
    }

    @Test
    public void converts_to_list_of_map_of_date() {
        DataTable table = TableParser.parse("|Birth Date|Death Cal|\n|1957-05-10|1979-02-02|\n", PARAMETER_INFO);
        List<Map<String, Date>> converted = table.asMaps(String.class, Date.class);
        assertEquals(sidsBirthday(), converted.get(0).get("Birth Date"));
    }

    @Test
    public void converts_to_list_of_map_of_string() {
        DataTable table = TableParser.parse("|Birth Date|Death Cal|\n|1957-05-10|1979-02-02|\n", null);
        List<Map<String, String>> converted = table.asMaps(String.class, String.class);
        assertEquals("1957-05-10", converted.get(0).get("Birth Date"));
    }

    private Date sidsBirthday() {
        Calendar sidsBirthday = Calendar.getInstance(Locale.US);
        sidsBirthday.set(1957, 4, 10, 0, 0, 0);
        sidsBirthday.set(Calendar.MILLISECOND, 0);
        return sidsBirthday.getTime();
    }

    private Calendar sidsDeathcal() {
        Calendar sidsDeathcal = Calendar.getInstance(Locale.US);
        sidsDeathcal.set(1979, 1, 2, 0, 0, 0);
        sidsDeathcal.set(Calendar.MILLISECOND, 0);
        return sidsDeathcal;
    }

    @Test
    public void converts_distinct_tostring_objects_correctly() {
        DataTable table = TableParser.parse("|first|second|\n|row1.first|row1.second|\n|row2.first|row2.second|\n", null);
        List<ContainsTwoFromStringableFields> converted = table.asList(ContainsTwoFromStringableFields.class);

        List<ContainsTwoFromStringableFields> expected = Arrays.asList(
                new ContainsTwoFromStringableFields(new FirstFromStringable("row1.first"), new SecondFromStringable("row1.second")),
                new ContainsTwoFromStringableFields(new FirstFromStringable("row2.first"), new SecondFromStringable("row2.second"))
        );

        assertEquals(expected, converted);
    }

    public static class ContainsTwoFromStringableFields {
        private FirstFromStringable first;
        private SecondFromStringable second;

        public ContainsTwoFromStringableFields(FirstFromStringable first, SecondFromStringable second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ContainsTwoFromStringableFields that = (ContainsTwoFromStringableFields) o;

            if (first != null ? !first.equals(that.first) : that.first != null) return false;
            if (second != null ? !second.equals(that.second) : that.second != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = first != null ? first.hashCode() : 0;
            result = 31 * result + (second != null ? second.hashCode() : 0);
            return result;
        }
    }

    public static class FirstFromStringable {
        private final String value;

        public FirstFromStringable(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FirstFromStringable that = (FirstFromStringable) o;

            if (value != null ? !value.equals(that.value) : that.value != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }

    public static class SecondFromStringable {
        private final String value;

        public SecondFromStringable(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SecondFromStringable that = (SecondFromStringable) o;

            if (value != null ? !value.equals(that.value) : that.value != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }
}
