package org.concordion.cubano.driver.http;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A wrapper around "javax.xml" for simplifying the parsing XML strings.
 *
 * @author Andrew Sumner
 */
public class XmlReader implements ResponseReader {
    private final Document document;

    /**
     * An xml reader.
     *
     * @param xml XML string
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public XmlReader(String xml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        builder = domFactory.newDocumentBuilder();


        InputSource src = new InputSource();
        src.setCharacterStream(new StringReader(xml));

        document = builder.parse(src);
    }

    /**
     * An xml reader.
     *
     * @param node An xml node
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     */
    public XmlReader(Node node) throws ParserConfigurationException, SAXException, IOException, TransformerFactoryConfigurationError, TransformerException {
        // some Nodes cannot be cast as (Document) node;
        // ClassCastException: com.sun.xml.internal.messaging.saaj.soap.impl.ElementImpl cannot be cast to org.w3c.dom.Document
        // this constructor is not ideal, however manages the process for now.
        this(convertXmlNodeToString(node));

    }

    private static String convertXmlNodeToString(Node node) throws TransformerFactoryConfigurationError, TransformerException {
        DOMSource domSource = new DOMSource(node);
        Transformer transformer;
        transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter sw = new StringWriter();
        StreamResult sr = new StreamResult(sw);
        transformer.transform(domSource, sr);
        return sw.toString();
    }

    /**
     * Evaluate an XPath expression in the specified context and return the result as the specified type..
     *
     * @param selector search path
     * @return Node
     */
    private Node evaluate(String selector) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        return (Node) xPath.evaluate(selector, document, XPathConstants.NODE);
    }

    /**
     * Search a XML element's children for the requested element and returns the text content.
     * <p>
     * <p>
     * Example:
     * <p>
     * <pre>
     * {@literal <}serviceResponse returnLength="1">
     *   {@literal <}output name="documentId">idd_CD1C398E-1F25-436D-B76A-9E293BB426F5{@literal <}/output>
     * {@literal <}/serviceResponse>
     *
     * String documentId = reader.getXmlReader().textContent("//{@literal *}/output[@name='documentId']");
     * </pre>
     * </p>
     *
     * @param selector search path
     * @return Node
     */
    public String textContent(String selector) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        Node node = evaluate(selector);

        if (node == null) {
            return null;
        }

        return node.getTextContent();
    }

    @Override
    public String asPrettyString() {
        return prettyFormat(getXmlString(document), 2);

    }

    /**
     * Converts the XML document to a nicely formatted string.
     *
     * @param input  XML string
     * @param indent number of spaces to indent each line
     * @return Formatted XML
     */
    public static String prettyFormat(String input, int indent) {
        try {
            Source xmlInput = new StreamSource(new StringReader(input));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(indent));
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Throwable t) {
            return input;
        }
    }

    private String getXmlString(Node node) {
        try {
            DOMSource domSource = new DOMSource(node);
            Transformer transformer;
            transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter sw = new StringWriter();
            StreamResult sr = new StreamResult(sw);
            transformer.transform(domSource, sr);
            return sw.toString();
        } catch (TransformerFactoryConfigurationError | TransformerException e) {
            DOMImplementationLS domImplementation = (DOMImplementationLS) document.getImplementation();
            LSSerializer lsSerializer = domImplementation.createLSSerializer();
            return lsSerializer.writeToString(document);
        }
    }

    private boolean isNodeNamed(Node node, String name) {
        String nodeName = node.getNodeName();
        int index = nodeName.indexOf(":");

        if (index >= 0) {
            nodeName = nodeName.substring(index + 1);
        }

        return nodeName.equalsIgnoreCase(name);
    }

    /**
     * Deserialize the XML into specified class. If the XML is a soap envelope then will deserialize the first node found in the body element.
     *
     * @param <T>   The type of the desired class
     * @param clazz Class of object to deserialize to
     * @return Populated class of the desired type
     * @throws JAXBException
     */
    @SuppressWarnings("unchecked")
    public <T> T from(Class<T> clazz) throws JAXBException {
        Node parse = document;

        if (document.hasChildNodes()) {
            Node node = document.getFirstChild();

            if (isNodeNamed(node, "Envelope")) {
                Node child = node.getFirstChild();

                while (child != null) {
                    if (isNodeNamed(child, "Body")) {
                        parse = child.getFirstChild();
                        break;
                    }

                    child = child.getNextSibling();
                }
            }
        }

        StringReader stream = new StringReader(getXmlString(parse));
        T result;

        try {
            JAXBContext jaxbc = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = jaxbc.createUnmarshaller();
            result = (T) unmarshaller.unmarshal(stream);
        } catch (JAXBException e) {
            throw e;
        }

        return result;
    }
}
