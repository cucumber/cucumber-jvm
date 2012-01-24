package cucumber.cli;

import cucumber.formatter.FormatterFactory;
import cucumber.formatter.MultiFormatter;


public class FormatCreator implements Formatable {
    private FormatterFactory formatterFactory;
    private MultiFormatter multiFormatter;

    public FormatCreator(FormatterFactory factory, MultiFormatter multiFormatter) {
        formatterFactory = factory;
        this.multiFormatter = multiFormatter;
    }

    public void eachWithDestination(String format, String destination) {
        Object out = (destination == null) ? System.out : destination;
        multiFormatter.add(formatterFactory.createFormatter(format, out));
    }

    public MultiFormatter getMultiFormatter() {
        return multiFormatter;
    }
}
