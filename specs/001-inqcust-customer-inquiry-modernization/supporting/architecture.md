# architecture.md

## Current legacy flow

```text
Calling CICS Program
        |
        v
DFHCOMMAREA using INQCUSTZ
        |
        v
INQCUST.cbl
        |
        +--> CUSTDB2 / SQLCA
        |
        +--> DB2 CUSTOMER table
        |
        +--> ABNDPROC for selected error handling
        |
        v
DFHCOMMAREA response
```

## POC target flow

```text
Swagger / Postman / Demo Client
        |
        v
CustomerInquiryController
        |
        v
CustomerInquiryService
        |
        +--> LookupModeResolver
        +--> CustomerRepository interface
        |       |
        |       v
        |   MockCustomerRepository
        |       |
        |       v
        |   mock-data/customer-records.json
        |
        +--> CustomerMapper
        +--> RiskAssessmentService
        +--> LegacyStatusFactory
        |
        v
CustomerInquiryResponse JSON
```

## Future production extension

```text
CustomerRepository interface
        |
        +--> MockCustomerRepository       # POC
        +--> Db2CustomerRepository        # Future direct Db2
        +--> ZosConnectCustomerRepository # Future z/OS Connect
        +--> CicsCustomerRepository       # Future CICS transaction wrapper
```

## Component responsibilities

| Component | Responsibility |
|---|---|
| CustomerInquiryController | HTTP endpoint, validation, delegates to service. |
| CustomerInquiryService | Orchestration and legacy behavior implementation. |
| LookupModeResolver | Converts customerNumber into SPECIFIC/RANDOM/LATEST. |
| CustomerRepository | Data source abstraction. |
| MockCustomerRepository | Loads and queries local JSON data. |
| CustomerMapper | Maps CustomerRecord to CustomerResponse. |
| LegacyDateConverter | Converts YYYYMMDD integers to LocalDate. |
| RiskAssessmentService | Adds modernization enhancement using credit score/status/review date. |
| LegacyStatusFactory | Creates legacy-style success/failure status response. |
| GlobalExceptionHandler | Converts validation/system errors into JSON responses. |
