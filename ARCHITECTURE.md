# Architecture Documentation

## 1. Hexagonal Architecture (Ports & Adapters)

This diagram shows how the application is structured. The **Domain** (core logic) is isolated from the **Infrastructure** (adapters) via **Ports** (interfaces).

```mermaid
graph TD
    subgraph Infrastructure [Adapters Layer]
        CLI[Console Application]
        DB_Adapter[PostgresConfigRepository]
        XML_Adapter[DomInvoiceRepository]
    end

    subgraph Application [Ports Layer]
        InPort((Input Port))
        OutPort_DB((ConfigurationRepo))
        OutPort_XML((InvoiceRepo))
    end

    subgraph Domain [Domain Layer]
        Service[AddendaService]
    end

    CLI -->|Calls| Service
    Service -->|Implements| InPort
    
    Service -->|Uses| OutPort_DB
    Service -->|Uses| OutPort_XML

    DB_Adapter -->|Implements| OutPort_DB
    XML_Adapter -->|Implements| OutPort_XML
```

## 2. Sequence Diagram (Execution Flow)

This diagram illustrates the flow of control when the application runs.

```mermaid
sequenceDiagram
    participant User
    participant CLI as ConsoleApplication
    participant Service as AddendaService
    participant InvoiceRepo as DomInvoiceRepository
    participant ConfigRepo as PostgresConfigRepository
    participant DB as Supabase(PostgreSQL)

    User->>CLI: java -jar ... file.xml ID
    CLI->>ConfigRepo: Initialize
    CLI->>InvoiceRepo: Initialize
    CLI->>Service: addToyotaAddenda(file, ID)
    
    activate Service
    Service->>InvoiceRepo: load(file)
    InvoiceRepo-->>Service: Document (DOM)
    
    Service->>ConfigRepo: getString("CFDI_ADDENDA")
    ConfigRepo->>DB: SELECT CONFIG_VALUE
    DB-->>ConfigRepo: "cfdi:Addenda"
    ConfigRepo-->>Service: "cfdi:Addenda"
    
    Service->>InvoiceRepo: removeAddenda(doc, "cfdi:Addenda")
    
    Service->>InvoiceRepo: getAttribute(doc, "Total")
    InvoiceRepo-->>Service: "100.00"
    
    Service->>InvoiceRepo: addToyotaAddenda(doc, ID, Total, Config)
    
    Service->>InvoiceRepo: save(doc, file)
    deactivate Service
    
    CLI-->>User: Success Message
```

## 3. State Diagram (Invoice Processing)

This diagram represents the states a specific XML Invoice goes through during the process.

```mermaid
stateDiagram-v2
    [*] --> Loaded: File Read
    Loaded --> Validated: Check Existence & Permissions
    Validated --> Parsed: XML Parsed to DOM
    Parsed --> Cleaned: Old Addenda Removed
    Cleaned --> Enriching: Extracting Attributes (Total, NoId)
    
    state Enriching {
        [*] --> GetTotal
        GetTotal --> GetNoIdentificacion
        GetNoIdentificacion --> [*]
    }

    Enriching --> Modiified: New Addenda Appended
    Modiified --> Saved: XML Written to Disk
    Saved --> [*]

    Parsed --> Error: XML Parse Error
    Enriching --> Error: Missing Attributes
    Error --> [*]
```
