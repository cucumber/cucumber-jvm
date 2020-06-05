package io.cucumber.java;

import io.cucumber.cucumberexpressions.GeneratedExpression;
import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Register parameter type.
 * <p>
 * The name of the method is used as the name of the
 * {@link io.cucumber.cucumberexpressions.ParameterType}.
 * <p>
 * The method must have one of these signatures. The number of {@code String}
 * parameters must match the number of capture groups in the regular expression.
 * <ul>
 * <li>{@code String -> Author}</li>
 * <li>{@code String, String -> Author}</li>
 * <li>{@code String, String, ect -> Author}</li>
 * <li>{@code String... -> Author}</li>
 * </ul>
 * NOTE: {@code Author} is an example of the type of the parameter type.
 * {@link io.cucumber.cucumberexpressions.ParameterType#getType()}
 *
 * @see io.cucumber.cucumberexpressions.ParameterType
 * @see <a href=https://cucumber.io/docs/cucumber/cucumber-expressions>Cucumber
 *      Expressions</a>
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@API(status = API.Status.STABLE)
public @interface ParameterType {

    /**
     * Regular expression.
     * <p>
     * Describes which patterns match this parameter type. If the expression
     * includes capture groups their captured strings will be provided as
     * individual arguments.
     *
     * @return a regular expression.
     * @see    io.cucumber.cucumberexpressions.ParameterType#getRegexps()
     */
    String value();

    /**
     * Name of the parameter type.
     * <p>
     * This is used as the type name in typed expressions. When not provided
     * this will default to the name of the annotated method.
     *
     * @return human readable type name
     * @see    io.cucumber.cucumberexpressions.ParameterType#getName()
     */
    String name() default "";

    /**
     * Indicates whether or not this is a preferential parameter type when
     * matching text against a RegularExpression. In case there are multiple
     * parameter types with a regexp identical to the capture group's regexp, a
     * preferential parameter type will win. If there are more than 1
     * preferential ones, an error will be thrown.
     *
     * @return true if this is a preferential type
     * @see    io.cucumber.cucumberexpressions.ParameterType#preferForRegexpMatch()
     */
    boolean preferForRegexMatch() default false;

    /**
     * Indicates whether or not this is a parameter type that should be used for
     * generating {@link GeneratedExpression}s from text. Typically, parameter
     * types with greedy regexps should return false.
     *
     * @return true is this parameter type is used for expression generation
     * @see    io.cucumber.cucumberexpressions.ParameterType#useForSnippets()
     */
    boolean useForSnippets() default false;

    /**
     * Indicates whether or not this parameter provides a strong type hint when
     * considering a regular expression match. If so, the type hint provided by
     * the method arguments will be ignored. If not, when both type hints are in
     * agreement, this parameter type's transformer will be used. Otherwise
     * parameter transformation for a regular expression match will be handled
     * by {@link DefaultParameterTransformer}.
     *
     * @return true if this parameter type provides a type hint when considering
     *         a regular expression match
     */
    boolean useRegexpMatchAsStrongTypeHint() default false;

}
