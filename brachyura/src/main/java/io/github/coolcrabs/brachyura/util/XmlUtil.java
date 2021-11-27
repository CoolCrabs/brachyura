package io.github.coolcrabs.brachyura.util;

import java.io.Writer;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class XmlUtil {
    private XmlUtil() {
    }

    public static FormattedXMLStreamWriter newStreamWriter(Writer writer) {
        try {
            return new FormattedXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(writer));
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public static class FormattedXMLStreamWriter implements XMLStreamWriter, AutoCloseable {
        final XMLStreamWriter parent;
        int indent = 0;

        public FormattedXMLStreamWriter(XMLStreamWriter parent) {
            this.parent = parent;
        }

        public void indent() {
            ++indent;
        }

        public void unindent() {
            --indent;
        }

        public void newline() throws XMLStreamException {
            parent.writeCharacters("\n");
            for (int i = 0; i < indent; i++) {
                parent.writeCharacters("    ");
            }
        }

        @Override
        public void writeStartElement(String localName) throws XMLStreamException {
            parent.writeStartElement(localName);
        }

        @Override
        public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
            parent.writeStartElement(namespaceURI, localName);
        }

        @Override
        public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
            parent.writeStartElement(prefix, localName, namespaceURI);
        }

        @Override
        public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
            parent.writeEmptyElement(namespaceURI, localName);
        }

        @Override
        public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
            parent.writeEmptyElement(prefix, localName, namespaceURI);
        }

        @Override
        public void writeEmptyElement(String localName) throws XMLStreamException {
            parent.writeEmptyElement(localName);
        }

        @Override
        public void writeEndElement() throws XMLStreamException {
            parent.writeEndElement();
        }

        @Override
        public void writeEndDocument() throws XMLStreamException {
            parent.writeEndDocument();
        }

        @Override
        public void close() throws XMLStreamException {
            parent.close();
        }

        @Override
        public void flush() throws XMLStreamException {
            parent.flush();
        }

        @Override
        public void writeAttribute(String localName, String value) throws XMLStreamException {
            parent.writeAttribute(localName, value);
        }

        @Override
        public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
                throws XMLStreamException {
            parent.writeAttribute(prefix, namespaceURI, localName, value);
        }

        @Override
        public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
            parent.writeAttribute(namespaceURI, localName, value);
        }

        @Override
        public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
            parent.writeNamespace(prefix, namespaceURI);
        }

        @Override
        public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
            parent.writeDefaultNamespace(namespaceURI);
        }

        @Override
        public void writeComment(String data) throws XMLStreamException {
            parent.writeComment(data);
        }

        @Override
        public void writeProcessingInstruction(String target) throws XMLStreamException {
            parent.writeProcessingInstruction(target);
        }

        @Override
        public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
            parent.writeProcessingInstruction(target, data);
        }

        @Override
        public void writeCData(String data) throws XMLStreamException {
            parent.writeCData(data);
        }

        @Override
        public void writeDTD(String dtd) throws XMLStreamException {
            parent.writeDTD(dtd);
        }

        @Override
        public void writeEntityRef(String name) throws XMLStreamException {
            parent.writeEntityRef(name);
        }

        @Override
        public void writeStartDocument() throws XMLStreamException {
            parent.writeStartDocument();
        }

        @Override
        public void writeStartDocument(String version) throws XMLStreamException {
            parent.writeStartDocument(version);
        }

        @Override
        public void writeStartDocument(String encoding, String version) throws XMLStreamException {
            parent.writeStartDocument(encoding, version);
        }

        @Override
        public void writeCharacters(String text) throws XMLStreamException {
            parent.writeCharacters(text);
        }

        @Override
        public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
            parent.writeCharacters(text, start, len);
        }

        @Override
        public String getPrefix(String uri) throws XMLStreamException {
            return parent.getPrefix(uri);
        }

        @Override
        public void setPrefix(String prefix, String uri) throws XMLStreamException {
            parent.setPrefix(prefix, uri);
        }

        @Override
        public void setDefaultNamespace(String uri) throws XMLStreamException {
            parent.setDefaultNamespace(uri);
        }

        @Override
        public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
            parent.setNamespaceContext(context);
        }

        @Override
        public NamespaceContext getNamespaceContext() {
            return parent.getNamespaceContext();
        }

        @Override
        public Object getProperty(String name) throws IllegalArgumentException {
            return parent.getProperty(name);
        }
    }

    public static void indent(XMLStreamWriter writer, int level) {
        for (int i = 0; i < level; i++) {
            try {
                writer.writeCharacters("    ");
            } catch (XMLStreamException e) {
                Util.sneak(e);
            }
        }
    }
}
