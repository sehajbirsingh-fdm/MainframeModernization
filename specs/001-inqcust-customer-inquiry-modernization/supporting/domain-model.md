# domain-model.md

## CustomerRecord
Internal model representing a DB2 CUSTOMER row or mock equivalent.

```java
public record CustomerRecord(
    String eyecatcher,
    String sortCode,
    String customerNumber,
    String title,
    String firstName,
    String lastName,
    Integer dateOfBirth,
    String phone,
    String addressLine1,
    String addressLine2,
    String city,
    String postcode,
    String country,
    String status,
    Integer createdDate,
    Integer creditScore,
    Integer creditScoreReviewDate
) {}
```

## CustomerResponse
External API representation.

```java
public record CustomerResponse(
    String eyecatcher,
    String sortCode,
    String customerNumber,
    String title,
    String firstName,
    String lastName,
    LocalDate dateOfBirth,
    String phone,
    AddressResponse address,
    CustomerStatus status,
    LocalDate createdDate,
    Integer creditScore,
    LocalDate creditScoreReviewDate
) {}
```

## AddressResponse
```java
public record AddressResponse(
    String line1,
    String line2,
    String city,
    String postcode,
    String country
) {}
```

## LegacyInquiryStatus
```java
public record LegacyInquiryStatus(
    String inquirySuccess,
    String inquiryFailCode,
    String message
) {}
```

## RiskAssessmentResponse
```java
public record RiskAssessmentResponse(
    RiskRating riskRating,
    boolean reviewRequired,
    List<String> reasons
) {}
```

## CustomerInquiryResponse
```java
public record CustomerInquiryResponse(
    LegacyInquiryStatus legacyStatus,
    LookupMode lookupMode,
    CustomerResponse customer,
    RiskAssessmentResponse riskAssessment
) {}
```

## Enums

```java
public enum CustomerStatus {
    ACTIVE, INACTIVE, SUSPENDED
}

public enum LookupMode {
    SPECIFIC, RANDOM, LATEST
}

public enum RiskRating {
    LOW, MEDIUM, HIGH
}
```
