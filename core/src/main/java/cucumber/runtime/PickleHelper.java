package cucumber.runtime;

import cucumber.messages.Pickles;
import cucumber.messages.Pickles.Pickle;
import cucumber.messages.Pickles.PickleStep;
import cucumber.messages.Pickles.PickleTable;
import cucumber.messages.Pickles.PickleTableCell;
import cucumber.messages.Pickles.PickleTableRow;
import cucumber.messages.Pickles.PickleTag;
import cucumber.messages.Sources.Location;

import java.util.List;

import static java.util.Arrays.asList;

// TODO: Test data builder - move to a test module that we exclude from release
public class PickleHelper {
    private PickleHelper() {
    }

    public static PickleStep step(String text) {
        return PickleStep.newBuilder()
            .setText(text)
            .addLocations(Location.newBuilder())
            .build();
    }

    public static PickleStep step(String text, PickleTable table) {
        return PickleStep.newBuilder()
            .setText(text)
            .setDataTable(table)
            .addLocations(Location.newBuilder())
            .build();
    }

    public static PickleStep step() {
        return step("a step");
    }

    public static Pickle pickle() {
        return pickle("some_file.feature", location());
    }

    public static Pickle pickle(String uri, Location... locations) {
        return pickle(uri, asList(locations));
    }

    public static Pickle pickle(String uri, List<Location> locations) {
        return Pickle.newBuilder()
            .setUri(uri)
            .setLanguage("en")
            .addAllLocations(locations)
            .build();
    }

    public static Pickle pickle(Location... locations) {
        return Pickle.newBuilder()
            .setUri("some_file.feature")
            .setLanguage("en")
            .addAllLocations(asList(locations))
            .build();
    }

    public static Pickle pickle(PickleStep... steps) {
        return pickle(asList(steps));
    }

    public static Pickle pickle(List<PickleStep> steps) {
        return Pickle.newBuilder()
            .addAllSteps(steps)
            .setLanguage("en")
            .addLocations(location())
            .build();
    }

    public static Location location() {
        return Location.newBuilder().build();
    }

    public static PickleTable table(PickleTableRow... rows) {
        return table(asList(rows));
    }

    public static PickleTable table(List<PickleTableRow> rows) {
        return PickleTable.newBuilder()
            .addAllRows(rows)
            .build();
    }

    public static PickleTableRow row(PickleTableCell... cells) {
        return PickleTableRow.newBuilder()
            .addAllCells(asList(cells))
            .build();
    }

    public static PickleTableCell cell(String value) {
        return PickleTableCell.newBuilder()
            .setValue(value)
            .setLocation(location())
            .build();
    }

    public static PickleTag tag(String name) {
        return PickleTag.newBuilder()
            .setName(name)
            .setLocation(location())
            .build();
    }

    public static Pickle pickleWithTags(PickleTag... tags) {
        return Pickle.newBuilder()
            .setUri("uri")
            .addAllTags(asList(tags))
            .addLocations(location())
            .build();
    }
}
