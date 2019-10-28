package io.cucumber.core.backend;

import org.apiguardian.api.API;

import java.net.URI;
import java.util.Collection;


@API(status = API.Status.STABLE)
public interface TestCaseState {
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
     * @param data      what to embed, for example an image.
     * @param mediaType what is the data? Using the
     * @see #embed(byte[], String, String)
     * @see #embed(byte[], String, String)
     * @deprecated use {@link TestCaseState#embed(byte[], String, String)} instead.
     */
    @Deprecated
    void embed(byte[] data, String mediaType);

    /**
     * Embeds data into the report(s).
     * <pre>
     * {@code
     * // Embed a screenshot. See your UI automation tool's docs for
     * // details about how to take a screenshot.
     * scenario.embed(pngBytes, "image/png", "Bartholomew and the Bytes of the Oobleck");
     * }
     * </pre>
     * <p>
     * To ensure reporting tools can understand what the data is a
     * {@code mediaType} must be provided. For example: {@code text/plain},
     * {@code image/png}, {@code text/html;charset=utf-8}.
     * <p>
     * Media types are defined in <a href= https://tools.ietf.org/html/rfc7231#section-3.1.1.1>RFC 7231 Section 3.1.1.1</a>.
     *
     * @param data      what to embed, for example an image.
     * @param mediaType what is the data?
     * @param name      embedding name
     */
    void embed(byte[] data, String mediaType, String name);

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
    URI getUri();

    /**
     * @return the line in the feature file of the Scenario. If this is a Scenario
     * from Scenario Outlines this will return the line of the example row in
     * the Scenario Outline.
     */
    Integer getLine();

}
