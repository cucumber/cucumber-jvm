package io.cucumber.java;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Register a data table type.
 * <p>
 * The signature of the method is used to determine which data table type is
 * registered:
 * <ul>
 * <li>{@code String -> Author} will register a
 * {@link io.cucumber.datatable.TableCellTransformer}</li>
 * <li>{@code Map<String, String> -> Author} will register a
 * {@link io.cucumber.datatable.TableEntryTransformer}</li>
 * <li>{@code List<String> -> Author} will register a
 * {@link io.cucumber.datatable.TableRowTransformer}</li>
 * <li>{@code DataTable -> Author} will register a
 * {@link io.cucumber.datatable.TableTransformer}</li>
 * </ul>
 * NOTE: {@code Author} is an example of the class you want to convert the table
 * to.
 *
 * @see io.cucumber.datatable.DataTableType
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@API(status = API.Status.STABLE)
public @interface DataTableType {

    /**
     * Replace these strings in the Datatable with empty strings.
     * <p>
     * A data table can only represent absent and non-empty strings. By
     * replacing a known value (for example [empty]) a data table can also
     * represent empty strings.
     * <p>
     * It is not recommended to use multiple replacements in the same table.
     *
     * @return strings to be replaced with empty strings.
     */
    String[] replaceWithEmptyString() default {};

}
