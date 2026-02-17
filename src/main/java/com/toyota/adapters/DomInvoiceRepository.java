package com.toyota.adapters;

import com.toyota.ports.ConfigurationRepository;
import com.toyota.ports.InvoiceRepository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

public class DomInvoiceRepository implements InvoiceRepository {

    @Override
    public Document load(String filePath) {
        try {
            File xmlFile = new File(filePath);
            if (!xmlFile.exists()) {
                throw new IllegalArgumentException("File not found: " + filePath);
            }
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            return documentBuilder.parse(xmlFile);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException("Error loading XML: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Document document, String filePath) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(filePath));
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new RuntimeException("Error saving XML: " + e.getMessage(), e);
        }
    }

    @Override
    public String getAttribute(Document document, String tagName, String attributeName) {
        NodeList nodes = document.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            Element element = (Element) nodes.item(0);
            if (element.hasAttribute(attributeName)) {
                return element.getAttribute(attributeName);
            }
        }
        return null; // Or throw exception based on strictness
    }

    @Override
    public void removeAddenda(Document document, String addendaTag) {
        NodeList oldAddenda = document.getElementsByTagName(addendaTag);
        if (oldAddenda.getLength() > 0) {
            Element root = document.getDocumentElement();
            root.removeChild(oldAddenda.item(0));
        }
    }

    @Override
    public void addToyotaAddenda(Document document, String shipmentId, String total, ConfigurationRepository config) {
        Element root = document.getDocumentElement();

        // Create new Addenda elements
        Element newAddenda = document.createElement(config.getString("CFDI_ADDENDA"));
        Element addToyota = document.createElement(config.getString("ADDENDA_TOYOTA"));
        addToyota.setAttribute("xmlns", config.getString("NAMESPACE_TOYOTA"));
        Element tmmgt = document.createElement(config.getString("TMMGT"));
        Element shipmentOrder = document.createElement(config.getString("SHIPMENT_ORDER"));

        shipmentOrder.setAttribute(config.getString("SHIPMENT_ID_ATTR"), shipmentId);
        shipmentOrder.setAttribute(config.getString("AMOUNT_ATTR"), total);

        tmmgt.appendChild(shipmentOrder);
        addToyota.appendChild(tmmgt);
        newAddenda.appendChild(addToyota);
        root.appendChild(newAddenda);
    }
}
