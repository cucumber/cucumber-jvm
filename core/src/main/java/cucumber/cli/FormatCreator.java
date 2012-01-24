package cucumber.cli;

import cucumber.formatter.FormatterFactory;
import cucumber.formatter.MultiFormatter;


public class FormatCreator implements Formatable {
    private FormatterFactory _formatterFactory;
    private MultiFormatter _multiFormatter;

    public FormatCreator(FormatterFactory $factory, MultiFormatter $multiFormatter) {
        _formatterFactory = $factory;
        _multiFormatter = $multiFormatter;
    }

    public void eachWithDestination(String $format, String $destination) {
        Object out = ($destination == null) ? System.out : $destination;
        _multiFormatter.add(_formatterFactory.createFormatter($format, out));
    }

    public MultiFormatter getMultiFormatter() {
        return _multiFormatter;
    }
}
