package io.cucumber.core.plugin;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Objects;
import java.util.regex.Pattern;

import static java.lang.Character.offsetByCodePoints;

class EscapingXmlStreamWriter implements AutoCloseable {

    private final XMLStreamWriter writer;

    public EscapingXmlStreamWriter(XMLStreamWriter writer) {
        this.writer = Objects.requireNonNull(writer);
    }

    @Override
    public void close() throws XMLStreamException {
        writer.close();
    }

    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        writer.writeStartDocument(encoding, version);
    }

    public void newLine() throws XMLStreamException {
        writer.writeCharacters("\n");
    }

    public void writeStartElement(String localName) throws XMLStreamException {
        writer.writeStartElement(localName);
    }

    public void writeEndElement() throws XMLStreamException {
        writer.writeEndElement();
    }

    public void writeEndDocument() throws XMLStreamException {
        writer.writeEndDocument();
    }

    public void flush() throws XMLStreamException {
        writer.flush();
    }

    public void writeEmptyElement(String localName) throws XMLStreamException {
        writer.writeEmptyElement(localName);
    }

    public void writeAttribute(String localName, String value) throws XMLStreamException {
        writer.writeAttribute(localName, escapeIllegalChars(value));
    }

    private static final Pattern CDATA_TERMINATOR_SPLIT = Pattern.compile("(?<=]])(?=>)");

    public void writeCData(String data) throws XMLStreamException {
        // https://stackoverflow.com/questions/223652/is-there-a-way-to-escape-a-cdata-end-token-in-xml
        for (String part : CDATA_TERMINATOR_SPLIT.split(data)) {
            // see https://www.w3.org/TR/xml/#dt-cdsection
            writer.writeCData(escapeIllegalChars(part));
        }
    }

    private static String escapeIllegalChars(String value) {
        boolean allAllowed = true;
        for (int i = 0; i < value.length(); i = offsetByCodePoints(value, i, 1)) {
            int codePoint = value.codePointAt(i);
            if (!isLegal(codePoint)) {
                allAllowed = false;
                break;
            }
        }
        if (allAllowed) {
            return value;
        }

        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i = offsetByCodePoints(value, i, 1)) {
            int codePoint = value.codePointAt(i);
            if (isLegal(codePoint)) {
                escaped.appendCodePoint(codePoint);
            } else {
                // see https://www.w3.org/TR/xml/#NT-CharRef
                escaped.append("&#").append(codePoint).append(';');
            }
        }
        return escaped.toString();
    }

    private static boolean isLegal(int codePoint) {
        // see https://www.w3.org/TR/xml/#charsets
        return codePoint == 0x9
                || codePoint == 0xA
                || codePoint == 0xD
                || (codePoint >= 0x20 && codePoint <= 0xD7FF)
                || (codePoint >= 0xE000 && codePoint <= 0xFFFD)
                || (codePoint >= 0x10000 && codePoint <= 0x10FFFF);
    }

}
