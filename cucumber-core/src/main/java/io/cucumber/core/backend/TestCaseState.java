package io.cucumber.core.backend;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.Collection;

@API(status = API.Status.STABLE)
public interface TestCaseState {

    /**
     * Return tags of this scenario.
     * 
     * @return tags of this scenario.
     */
    Collection<String> getSourceTagNames();

    /**
     * Returns the current status of this test case.
     * <p>
     * The test case status is calculated as the most severe status of the
     * executed steps in the testcase so far.
     *
     * @return the current status of this test case
     */
    Status getStatus();

    /**
     * Returns true when the scenario has failed.
     * <p>
     * This is implemented as {@code this.getStatus() == Status.FAILED}.
     * 
     * @return true if the scenario has failed.
     */
    boolean isFailed();

    /**
     * Attach data to the report(s).
     * 
     * <pre>
     * {@code
     * // Attach a screenshot. See your UI automation tool's docs for
     * // details about how to take a screenshot.
     * scenario.attach(pngBytes, "image/png", "Bartholomew and the Bytes of the Oobleck");
     * }
     * </pre>
     * <p>
     * To ensure reporting tools can understand what the data is a
     * {@code mediaType} must be provided. For example: {@code text/plain},
     * {@code image/png}, {@code text/html;charset=utf-8}.
     * <p>
     * Media types are defined in <a href=
     * https://tools.ietf.org/html/rfc7231#section-3.1.1.1>RFC 7231 Section
     * 3.1.1.1</a>.
     *
     * @param data      what to attach, for example an image.
     * @param mediaType what is the data?
     * @param name      attachment name
     */
    void attach(byte[] data, String mediaType, @Nullable String name);

    /**
     * Attach data to the report(s).
     * 
     * @param data      what to attach, for example html.
     * @param mediaType what is the data?
     * @param name      attachment name
     * @see             #attach(byte[], String, String)
     */
    void attach(String data, String mediaType, @Nullable String name);

    /**
     * Outputs some text into the report.
     *
     * @param text what to put in the report.
     * @see        #attach(byte[], String, String)
     */
    void log(String text);

    /**
     * Returns the name of the Scenario
     * 
     * @return the name of the Scenario
     */
    String getName();

    /**
     * Returns the id of the Scenario.
     * 
     * @return the id of the Scenario.
     */
    String getId();

    /**
     * Returns the uri of the Scenario.
     * 
     * @return the uri of the Scenario.
     */
    URI getUri();

    /**
     * Returns the language of the Scenario.
     * 
     * @return the language of the Scenario.
     */
    String getLanguage();

    /**
     * Returns the line in the feature file of the Scenario.
     * <p>
     * If this is a Scenario from Scenario Outlines this will return the line of
     * the example row in the Scenario Outline.
     * 
     * @return the line in the feature file of the Scenario.
     */
    Integer getLine();

}
