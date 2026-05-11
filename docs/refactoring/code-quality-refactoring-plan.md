# Code Quality Refactor Plan

## Summary

Goal: improve maintainability without changing visible behavior. Current tests pass: `mvn test` ran 12 tests successfully.

Main finding: the codebase is already moving in a good direction with service facades, repository interfaces, DTOs, message mappers, and file repository abstraction. The biggest remaining issues are oversized responsibility clusters in `Proposta`, large CLI/view interfaces, duplicated load-modify-save flows, and hidden singleton/global dependencies.

## SOLID / GRASP Findings

- **SRP / High Cohesion:** `Proposta` is doing too much: identity key generation, date parsing, field ordering, proposal validation, lifecycle transitions, subscription rules, participant parsing, and state history. See `Proposta.java:74`, `:121`, `:243`, `:275`, `:322`, `:341`, `:380`, `:485`.
- **OCP / Protected Variations:** `Proposta.validaCampo` uses a hard-coded switch on base field names at `Proposta.java:380`; adding proposal rules means editing `Proposta`. Move field/business rules into validator objects.
- **DIP / Low Coupling:** `BatchImportService` directly uses static `ObjectMapper` and `DefaultTypeValidator.INSTANCE` at `BatchImportService.java:39` and `:214`. Inject a parser/validator dependency instead.
- **ISP:** `IConfiguratoreView` is a large role interface with menu, catalog, proposal, import, archive, and error methods in one contract. See `IConfiguratoreView.java:47-110`. Split by workflow.
- **GRASP Controller / Indirection:** `Application` manually creates the entire graph and scheduler in one class at `Application.java:52-120`. Acceptable for a composition root, but extract scheduler/bootstrap factories if it grows further.
- **GRASP Information Expert:** Some logic rightly belongs in aggregates, but `Proposta` has crossed from "domain expert" into "god aggregate." Keep invariants there; move validation orchestration and parsing policies out.

## Key Refactors

- Extract proposal validation:
  - Create `PropostaBusinessValidator` with rule objects for required fields, participant count, subscription deadline, event date, and conclusion date.
  - Keep `Proposta` responsible for state invariants and mutations only.
  - `PropostaValidationService` becomes the orchestrator instead of simply delegating back to `Proposta`.

- Extract proposal identity and lookup:
  - Move `chiaveIdentita` into a `PropostaIdentityPolicy`.
  - Reuse it in `Bacheca`, `PropostaPublicationService`, `PropostaLifecycleService`, `IscrizioneService`, and `BatchImportService`.
  - Remove duplicated `trovaPropostaPersistita` logic from lifecycle and subscription services.

- Introduce repository transaction helpers:
  - Add a small application-level helper such as `BachecaUnitOfWork` / `CatalogoUnitOfWork` for load-modify-save.
  - Use it in publication, lifecycle, subscription, catalog field, and category services.
  - This reduces repeated `repo.load(); mutate; repo.save();` and makes concurrency/locking policy explicit.

- Split view contracts:
  - Break `IConfiguratoreView` into `ConfiguratoreMenuView`, `CatalogoConfigView`, `PropostaConfigView`, `ArchivioView`, and `ImportView`.
  - Controllers then depend only on the interface for the current workflow.

- Keep existing good patterns:
  - Repository interfaces are good DIP.
  - `AbstractFileRepository` is a useful Template Method style abstraction.
  - `PropostaService`, `CatalogoService`, `ConfiguratoreService`, and `FruitoreService` are valid Facades, but should stay thin.

## GoF Pattern Opportunities

- **State:** proposal lifecycle is already halfway there with `StatoProposta.canTransitionTo`. Either keep the enum as a compact State pattern or extract transition behavior into state/rule objects if lifecycle rules keep growing.
- **Strategy / Chain of Responsibility:** use for `TipoDato` validation and proposal business validation instead of switches and scattered validation code.
- **Observer / Domain Events:** lifecycle transitions can emit events like `PropostaConfermata`, `PropostaAnnullata`, `PropostaRitirata`; notification handling subscribes separately. This decouples lifecycle from notifications.
- **Command:** controller menu actions can become command handlers keyed by enum action, reducing large switch blocks over time.
- **Abstract Factory:** `FileRepositoryFactory` already resembles this, but remove the singleton and inject a `RepositoryFactory` instance.

## Test Plan

- Preserve existing green tests.
- Add focused tests for extracted proposal validation rules before moving code.
- Add tests for `PropostaIdentityPolicy` duplicate detection across bacheca, saved valid proposals, and batch imports.
- Add lifecycle event tests: transition occurs once, correct notification event is emitted, persistence still happens.
- Add view-interface refactor tests only where controller behavior changes.

## Assumptions

- Refactor should preserve CLI behavior and JSON file format.
- Java 17 and Maven structure remain unchanged.
- Prioritize behavior-preserving quality refactors over new features.
