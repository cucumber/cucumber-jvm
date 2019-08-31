package io.cucumber.java;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Register default data table entry transformer.
 * <p>
 * Valid method signatures are:
 * <ul>
 * <li>{@code Map<String,String>, Type -> Object}</li>
 * <li>{@code Object, Type -> Object}</li>
 * <li>{@code Map<String,String>, Type, TableCellByTypeTransformer -> Object}</li>
 * <li>{@code Object, Type, TableCellByTypeTransformer -> Object}</li>
 * </ul>
 *
 * @see io.cucumber.datatable.TableEntryByTypeTransformer
 * @see io.cucumber.datatable.DataTableType
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@API(status = API.Status.STABLE)
public @interface DefaultDataTableEntryTransformer {
    /**
     * Converts a data tables header headers to property names.
     * <p>
     * E.g. {@code Xml Http request} becomes {@code xmlHttpRequest}.
     *
     * @return true if conversion should be be applied, true by default.
     */
    boolean headersToProperties() default true;
}
