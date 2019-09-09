package io.cucumber.core.backend;

import io.cucumber.core.event.Status;
import org.apiguardian.api.API;

import java.util.Collection;

/**
 * Before or After Hooks that declare a parameter of this type will receive an instance of this class.
 * It allows writing text and embedding media into reports, as well as inspecting results (in an After block).
 * <p>
 * Note: This class is not intended to be used to create reports. To create custom reports use
 * the {@code io.cucumber.core.plugin.Plugin} class. The plugin system provides a much richer access to Cucumbers then
 * hooks after could provide. For an example see {@code io.cucumber.core.plugin.PrettyFormatter}.
 */
@API(status = API.Status.STABLE)
public interface Scenario {
    /**
     * @return source_tag_names.
     */
    Collection<String> getSourceTagNames();

    /**
     * @return the <em>most severe</em> status of the Scenario's Steps.
     */
    Status getStatus();

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
     * @deprecated use {@link Scenario#embed(byte[], String, String)} instead.
     */
    @Deprecated
    void embed(byte[] data, String mimeType);

    /**
     * Like {@link Scenario#embed(byte[], String)}, but with name for the embedding.
     *
     * @param data     what to embed, for example an image.
     * @param mimeType what is the data?
     * @param name     embedding name
     */
    void embed(byte[] data, String mimeType, String name);

    /**
     * Outputs some text into the report.
     *
     * @param text what to put in the report.
     */
    void write(String text);

    /**
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
     * from Scenario Outlines this will return the line of the example row in
     * the Scenario Outline.
     */
    Integer getLine();
}
