package com.toyota.domain;

import com.toyota.ports.ConfigurationRepository;
import com.toyota.ports.InvoiceRepository;
import org.w3c.dom.Document;

public class AddendaService {

    private final ConfigurationRepository configRepo;
    private final InvoiceRepository invoiceRepo;

    public AddendaService(ConfigurationRepository configRepo, InvoiceRepository invoiceRepo) {
        this.configRepo = configRepo;
        this.invoiceRepo = invoiceRepo;
    }

    public void addToyotaAddenda(String xmlFilePath, String shipmentIdPrefix) {
        try {
            // 1. Load Invoice
            Document document = invoiceRepo.load(xmlFilePath);

            // 2. Remove existing addenda
            invoiceRepo.removeAddenda(document, configRepo.getString("CFDI_ADDENDA"));

            // 3. Extract total
            String total = invoiceRepo.getAttribute(document, configRepo.getString("CFDI_COMPROBANTE"),
                    configRepo.getString("TOTAL_ATTR"));
            if (total == null) {
                throw new IllegalArgumentException("Could not find total in invoice.");
            }

            // 4. Extract NoIdentificacion
            String noIdentificacion = invoiceRepo.getAttribute(document, configRepo.getString("CFDI_CONCEPTO"),
                    configRepo.getString("NO_IDENTIFICACION_ATTR"));
            if (noIdentificacion == null) {
                throw new IllegalArgumentException("Could not find NoIdentificacion in first concept.");
            }

            // 5. Build Shipment ID
            String fullShipmentId = shipmentIdPrefix + "#" + noIdentificacion;

            // 6. Add new Addenda (Delegate to repo for DOM manipulation)
            // Ideally business logic should construct a POJO Addenda and pass it to repo,
            // but for this refactor we'll delegate DOM ops to the repo to keep it simple as
            // per plan.
            invoiceRepo.addToyotaAddenda(document, fullShipmentId, total, configRepo); // Passing configRepo is a bit
                                                                                       // leaky, but pragmatic for now.

            // 7. Save
            invoiceRepo.save(document, xmlFilePath);
            System.out.println("Addenda successfully added to " + xmlFilePath);

        } catch (Exception e) {
            throw new RuntimeException("Error processing addenda: " + e.getMessage(), e);
        }
    }
}
