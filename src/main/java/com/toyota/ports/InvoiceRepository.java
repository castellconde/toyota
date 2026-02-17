package com.toyota.ports;

import org.w3c.dom.Document;

public interface InvoiceRepository {
    Document load(String filePath);

    void save(Document document, String filePath);

    String getAttribute(Document document, String tagName, String attributeName);

    void removeAddenda(Document document, String addendaTag);

    void addToyotaAddenda(Document document, String shipmentId, String total,
            com.toyota.ports.ConfigurationRepository config);
}
