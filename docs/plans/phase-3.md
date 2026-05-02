# Phase 3 — Package-by-Domain Refactoring

## Goal

Reorganise the flat package-by-layer structure into a package-by-domain structure.
The single domain in this service is `rates`. Cross-cutting infrastructure lives in `shared`.
No behaviour changes — pure rename/move refactoring.

## Before

```
com.shipmonk.testingday
├── config/AppConfig.java
├── controller/ExchangeRatesController.java
├── converter/RatesMapConverter.java
├── dto/RatesResponse.java
├── dto/ErrorResponse.java
├── entity/ExchangeRateSnapshot.java
├── exception/ExchangeRateException.java
├── exception/GlobalExceptionHandler.java
├── exception/InvalidDateException.java
├── exception/ProviderException.java
├── exception/RatesNotFoundException.java
├── exception/TransientProviderException.java
├── provider/ExchangeRateProvider.java
├── provider/ExchangeRates.java
├── provider/fixer/FixerExchangeRateProvider.java
├── provider/fixer/dto/FixerRatesResponse.java
├── provider/fixer/dto/FixerErrorDetail.java
├── repository/ExchangeRateSnapshotRepository.java
└── service/ExchangeRatesService.java
```

## After

```
com.shipmonk.testingday
├── TestingdayExchangeRatesApplication.java          (unchanged)
│
├── rates/                                           ← domain package
│   ├── controller/ExchangeRatesController.java
│   ├── service/ExchangeRatesService.java
│   ├── repository/ExchangeRateSnapshotRepository.java
│   ├── entity/ExchangeRateSnapshot.java
│   ├── converter/RatesMapConverter.java
│   ├── dto/RatesResponse.java
│   ├── exception/
│   │   ├── ExchangeRateException.java
│   │   ├── InvalidDateException.java
│   │   ├── RatesNotFoundException.java
│   │   ├── ProviderException.java
│   │   └── TransientProviderException.java
│   └── provider/
│       ├── ExchangeRateProvider.java
│       ├── ExchangeRates.java
│       └── fixer/
│           ├── FixerExchangeRateProvider.java
│           └── dto/
│               ├── FixerRatesResponse.java
│               └── FixerErrorDetail.java
│
└── shared/                                          ← cross-cutting concerns
    ├── GlobalExceptionHandler.java
    ├── ErrorResponse.java
    └── AppConfig.java
```

## What goes where and why

### `rates/` — everything that is specific to exchange-rate business logic

All layers (controller, service, repository, entity, converter, dto, provider) stay inside
the domain. Domain-specific exceptions (`InvalidDateException`, `RatesNotFoundException`,
`ProviderException`, `TransientProviderException`) belong here because they express what can
go wrong *in this domain*.

### `shared/` — cross-cutting concerns with no domain knowledge

- **`GlobalExceptionHandler`** — a web-layer concern that maps domain exceptions to HTTP
  responses. It belongs to no single domain; if a second domain were added, the same handler
  would cover it.
- **`ErrorResponse`** — a generic API error envelope, not exchange-rate-specific.
- **`AppConfig`** — infrastructure configuration (RestTemplate bean), pure plumbing.

## Test reorganisation

Unit tests move to mirror their subject's new package:

```
src/test/java/com/shipmonk/testingday/
├── TestingdayExchangeRatesApplicationTests.java     (unchanged)
├── ExchangeRatesIT.java                             (stays at root — spans all layers)
└── rates/
    ├── controller/ExchangeRatesControllerTest.java
    ├── service/ExchangeRatesServiceTest.java
    ├── converter/RatesMapConverterTest.java
    └── provider/fixer/FixerExchangeRateProviderTest.java
```

`ExchangeRatesIT` stays at the root package because it is a full-stack integration test
that spans the controller, service, repository, and provider layers simultaneously.

## Implementation steps

1. Create new directory tree under `rates/` and `shared/`.
2. Write each file at its new path with the updated `package` declaration and updated imports.
3. Delete old source directories (`controller/`, `service/`, `repository/`, `entity/`,
   `converter/`, `dto/`, `exception/`, `provider/`, `config/`).
4. Move unit test files to their new paths with updated `package` declarations and imports.
5. Run `./mvnw verify` to confirm all unit and integration tests pass.
