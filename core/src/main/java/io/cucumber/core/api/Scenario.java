package io.cucumber.core.api;

import cucumber.api.Result;

import java.util.Collection;

/**
 * Before or After Hooks that declare a parameter of this type will receive an instance of this class.
 * It allows writing text and embedding media into reports, as well as inspecting results (in an After block).
 */
public interface Scenario {
    /**
     * @return source_tag_names. Needed for compatibility with Capybara.
     */
    Collection<String> getSourceTagNames();

    /**
     * @return the <em>most severe</em> status of the Scenario's Steps.
     */
    Result.Type getStatus();

    /**
     * @return true if and only if {@link #getStatus()} returns "failed"
     */
    boolean isFailed();

    /**
     * Embeds data into the report(s). Some reporters (such as the progress one) don't embed data, but others do (html and json).
     * Example:
     *
     * <pre>
     * {@code
     * // Embed a screenshot. See your UI automation tool's docs for
     * // details about how to take a screenshot.
     * scenario.embed(pngBytes, "image/png");
     * }
     * </pre>
     *
     * @param data     what to embed, for example an image.
     * @param mimeType what is the data?
     */
    void embed(byte[] data, String mimeType);

    /**
     * Outputs some text into the report.
     *
     * @param text what to put in the report.
     */
    void write(String text);

    /**
     *
     * @return the name of the Scenario
     */
    String getName();

    /**
     * @return the id of the Scenario.
     */
    String getId();

    /**
     * @return the uri of the Scenario.
     */
    String getUri();

    /**
     * @return the line in the feature file of the Scenario. If this is a Scenario
     * from Scenario Outlines this wil return the line of the example row in
     * the Scenario Outline.
     */
    Integer getLine();
}
