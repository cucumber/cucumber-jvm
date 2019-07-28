package io.cucumber.java;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Register a data table type.
 *
 * Supports TableCellTransformer: String -> T
 * Supports TableEntryTransformer: Map<String, String> -> T
 * Supports TableRowTransformer: List<String> -> T
 * Supports TableTransformer: DataTable -> T
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@API(status = API.Status.STABLE)
public @interface DataTableType {

}
