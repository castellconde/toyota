package com.toyota.adapters;

import com.toyota.domain.AddendaService;
import com.toyota.ports.ConfigurationRepository;
import com.toyota.ports.InvoiceRepository;

/**
 * Main entry point following Hexagonal Architecture.
 * Wires dependencies and executes the use case.
 */
public class ConsoleApplication {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err
                    .println("Usage: java com.toyota.adapters.ConsoleApplication <xml-file-path> <shipment-id-prefix>");
            System.exit(1);
        }

        String xmlFilePath = args[0];
        String shipmentIdPrefix = args[1];

        // 1. Initialize Adapters (Infrastructure)
        ConfigurationRepository configRepo = null;
        try {
            configRepo = new PostgresConfigurationRepository();
        } catch (Exception e) {
            System.err.println("Failed to initialize database configuration: " + e.getMessage());
            System.exit(1);
        }

        InvoiceRepository invoiceRepo = new DomInvoiceRepository();

        // 2. Initialize Domain Service (Application)
        AddendaService addendaService = new AddendaService(configRepo, invoiceRepo);

        // 3. Execute Use Case
        try {
            addendaService.addToyotaAddenda(xmlFilePath, shipmentIdPrefix);
        } catch (Exception e) {
            System.err.println("Error processing request: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
