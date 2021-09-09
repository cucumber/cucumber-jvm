package io.cucumber.java;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Register default data table cell transformer.
 * <p>
 * Valid method signatures are:
 * <ul>
 * <li>{@code String, Type -> Object}</li>
 * <li>{@code Object, Type -> Object}</li>
 * </ul>
 *
 * @see io.cucumber.datatable.TableCellByTypeTransformer
 * @see io.cucumber.datatable.DataTableType
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@API(status = API.Status.STABLE)
public @interface DefaultDataTableCellTransformer {

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
