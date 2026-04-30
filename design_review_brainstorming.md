# Design Review Brainstorming

## Project-Level Summary

This project already has a recognizable layered structure and several good design instincts: domain classes are separated from controllers, repositories hide file persistence behind interfaces, services coordinate use cases, and the CLI is accessed through view contracts. The design is stronger than a single procedural console program.

The main design weaknesses are not catastrophic. They are mostly cohesion and boundary issues:

- [recommended] Several classes have grown into "workflow hubs", especially `Application`, `ConsoleUI`, `ConfiguratoreController`, `PropostaService`, `StateTransitionService`, and `BatchImportService`.
- [recommended] The domain model contains persistence details through Jackson annotations. This is acceptable for a small academic CLI, but it weakens clean MVC and domain independence.
- [recommended] Business validation is duplicated or spread across `Proposta`, `PropostaService`, `IscrizioneService`, `StateTransitionService`, `ConsoleUI`, and import logic.
- [optional] Many operations return raw domain entities and strings directly to the view. A larger project would benefit from presenters or view models.
- [risky/overengineering] A full enterprise architecture rewrite would be unnecessary now. The current structure can be improved incrementally.

## MVC Architecture Analysis

### Current Layer Mapping

| Layer | Current classes/packages | Notes |
|---|---|---|
| Model - Domain | `it.unibs.ingsoft.domain` | Contains entities, value objects, enums, validation helpers, state logic, and notification containers. |
| Model - Application/use cases | `it.unibs.ingsoft.application`, `it.unibs.ingsoft.application.batch` | Coordinates domain objects and repositories. This is part of the "model" in a broad MVC interpretation. |
| View | `presentation.view.contract`, `presentation.view.cli.ConsoleUI` | Defines input/output contracts and the concrete console UI. |
| Controller | `presentation.controller` and part of `composition.Application` | Handles menu flows and user actions. `Application` also acts as top-level controller. |
| Persistence/Infrastructure | `persistence.api`, `persistence.impl` | Not MVC itself; it supports the model through repositories. |
| Composition/Bootstrap | `composition` | Wires dependencies and starts the application. |

### MVC Mixing Hotspots

- [recommended] `Application` mixes composition root, top-level login menu, session routing, data path configuration, and midnight scheduling. Split into `ApplicationBootstrap`, `MainMenuController` or `SessionController`, and `StateTransitionScheduler`.
- [recommended] `ConsoleUI` mixes rendering, input parsing, domain-specific proposal forms, inline type validation, and selection widgets. Keep it as a console adapter, but move proposal form orchestration into a `ProposalFormController` or `ProposalFormPresenter`.
- [recommended] Configurator proposal flows and catalog flows can put pressure on controller cohesion. Controllers are allowed to coordinate flow, but formatting labels and list construction can move to view/presenter methods.
- [recommended] `PropostaService` contains use-case orchestration, validation rules, duplicate detection, parsing, error text generation, and in-memory session state for valid-but-unpublished proposals. Extract validation rules and a session draft store.
- [recommended] `StateTransitionService` changes states and also builds notification messages. Move message formatting to a notification factory/presenter.
- [optional] `IOutputView` and `IInputView` depend directly on domain classes (`Proposta`, `Categoria`, `Campo`). This is convenient, but it couples view contracts to domain shape. Use view models if the UI grows.

## GRASP and SOLID Analysis

### GRASP

- **Information Expert:** Generally good in `Catalogo`, `Categoria`, `Proposta`, and `SpazioPersonale`, which hold data and enforce some related invariants. Weak spot: proposal date rules live mostly in `PropostaService`, not near a dedicated policy object.
- **Controller:** The controller classes are explicit, which is good. `ConfiguratoreController` is too broad and handles too many system events.
- **Creator:** `Application` and services create the correct objects, but `Application` creates almost everything and also runs the program.
- **Low Coupling:** Repository interfaces and view contracts help. Coupling increases where view contracts expose domain entities, services depend on concrete services, and domain depends on Jackson.
- **High Cohesion:** Strong for small DTOs and simple entities. Weak for `ConsoleUI`, `PropostaService`, `BatchImportService`, `ConfiguratoreController`, and `Application`.
- **Polymorphism:** Used lightly through `TypeValidator`, `ProposalFieldValidator`, and enum-specific `StatoProposta.canTransitionTo`.
- **Pure Fabrication:** Services and repositories are useful fabrications. More such helpers would improve validation, notification formatting, and scheduling.
- **Indirection/Protected Variations:** Repositories and view interfaces are good protection points. Persistence mapping and clock handling are less protected.

### SOLID

- **SRP:** Most frequent issue. Several classes have multiple reasons to change: UI layout, workflow, validation, persistence, and formatting are sometimes combined.
- **OCP:** Adding new validation rules, field types, import formats, or proposal states would require editing central classes. Strategy/specification objects would improve this.
- **LSP:** Mostly fine. `Persona` subclasses are simple and final; repository interfaces are narrow enough.
- **ISP:** Good intent with `IInputView` and `IOutputView`, but both are still large and domain-specific. Controllers often depend on the composite `IAppView`.
- **DIP:** Good for repositories. Less strong where services depend on concrete services (`StateTransitionService` -> `NotificationService`) or static/global helpers (`AppConstants.clock`, `DefaultTypeValidator.INSTANCE`).

## Existing and Suggested Design Patterns

### Existing Patterns

- **Repository:** `ICatalogoRepository`, `IBachecaRepository`, `ICredenzialiRepository`, `ISpazioPersonaleRepository` with file-based implementations. This is a good infrastructure boundary.
- **Template Method / reusable abstract superclass:** `AbstractFileRepository` centralizes JSON load/save behavior for concrete repositories.
- **Strategy-like validators:** `TypeValidator` and `ProposalFieldValidator` allow validation behavior to be passed around.
- **State-like enum behavior:** `StatoProposta` gives each enum constant its own transition rule.
- **DTO:** batch import records isolate JSON import shape from the domain model.
- **Composition Root:** `Application` wires the object graph, although it currently does more than composition.
- **Singleton-like instance:** `DefaultTypeValidator.INSTANCE` is a simple shared validator.

### Pattern Suggestions

- [recommended] **Strategy** for proposal validation rules. Useful because proposal validation is likely to grow and is already mixed into `PropostaService`.

```java
interface ProposalRule {
    Optional<String> validate(Proposta proposta);
}
```

- [recommended] **Observer / Domain Events** for state-change notifications. Useful because state transitions should not need to know how notification text is formatted or delivered.

```java
record PropostaConfermata(Proposta proposta) {}
interface DomainEventPublisher {
    void publish(Object event);
}
```

- [optional] **Factory or Builder** for `Proposta` creation from category/catalog/import data. Useful if creation keeps requiring ordered fields, defaults, parsed dates, and duplicate checks.
- [optional] **State pattern** or table-driven state machine if proposal lifecycle rules become more complex. Current enum behavior is sufficient for now.
- [optional] **Command** for menu actions in controllers. Useful only if menus continue growing or need reuse/testing. Not necessary for current size.
- [risky/overengineering] Full event sourcing, dependency injection framework, or layered DTO mapping everywhere. These would add ceremony without immediate payoff.

## Class-by-Class Review Table

| Class/File | Current responsibility | Design problems | GRASP/SOLID notes | Suggested refactoring | Possible patterns | Priority |
|---|---|---|---|---|---|---|
| `composition/Main.java` - `Main` | Minimal Java entry point. | No real issue. | SRP good; Creator delegated to `Application`. | Keep as is. | None needed. | optional |
| `composition/Application.java` - `Application` | Wires dependencies, starts scheduler, owns login/session loop. | Composition, controller flow, scheduler, data paths, and shutdown are combined. | SRP and High Cohesion weak; Controller role mixed with Creator. | [recommended] Extract `StateTransitionScheduler` and top-level menu/session controller; keep composition root focused on wiring. | Composition Root retained; Facade for application session. | recommended |
| `domain/AppConstants.java` - `AppConstants` | Global date/time formats, field-name constants, mutable test clock. | Global mutable `clock`; constants mix domain vocabulary and infrastructure date formatting. | DIP weak due static global dependency; Protected Variations weak for time. | [recommended] Inject `Clock` into services; keep field names in a domain vocabulary class. | None; possibly Value Object for field names. | recommended |
| `domain/Persona.java` - `Persona` | Base identity for users. | Abstract base is very small; inheritance may be heavier than roles need. | LSP fine; SRP good; GRASP Information Expert okay. | [optional] Keep; or replace with role value/object if roles grow. | None needed. | optional |
| `domain/Configuratore.java` - `Configuratore` | Configurator user role. | Marker subclass with no behavior. | SRP fine; possible speculative inheritance. | [optional] Keep for clarity; add behavior only if role-specific rules appear. | None. | optional |
| `domain/Fruitore.java` - `Fruitore` | Consumer user role. | Marker subclass with no behavior. | SRP fine; possible speculative inheritance. | [optional] Keep for clarity; add behavior only if role-specific rules appear. | None. | optional |
| `domain/Credenziali.java` - `Credenziali` | Stores configurator and fruitore credentials. | Plain text passwords; domain class has Jackson annotations; maps returned read-only but internal structure is persistence-shaped. | SRP mixed with persistence; security concern; DIP weak due annotations. | [recommended] Hash passwords; [optional] move JSON mapping to persistence DTO. | Repository already shields storage; Value Object for password hash. | recommended |
| `domain/Campo.java` - `Campo` | Immutable field definition with name, scope, type, mandatory flag. | Equality only by name ignores `tipo` and `tipoDato`; may surprise if same name appears in different contexts. | Information Expert good; LSP not relevant; SRP good. | [optional] Document identity by name or introduce `CampoId`. | Value Object. | optional |
| `domain/Categoria.java` - `Categoria` | Category aggregate containing specific fields. | Copy constructor uses `toList`, producing an unmodifiable list while mutating methods expect modifiable list; Jackson annotations in domain. | SRP mostly good; persistence coupling; possible behavioral bug from copy constructor. | [recommended] Ensure copied list is mutable (`new ArrayList<>(...)`); isolate Jackson later. | Aggregate root style. | recommended |
| `domain/Catalogo.java` - `Catalogo` | Catalog aggregate for base/common fields and categories. | `nomeEsistenteGlobale` ignores specific fields despite comments and use sites implying global uniqueness; Jackson annotations in domain. | Information Expert good but invariant enforcement incomplete; SRP persistence leakage. | [recommended] Include category-specific fields in global-name check if uniqueness is intended; clarify invariant. | Aggregate root; Specification for uniqueness optional. | recommended |
| `domain/Bacheca.java` - `Bacheca` | Serializable collection of proposals. | `addProposta` comment says APERTA but method does not enforce it. | Information Expert weak for invariant; SRP okay except Jackson. | [recommended] Enforce state or remove misleading precondition. | Aggregate root style. | recommended |
| `domain/ArchivioNotifiche.java` - `ArchivioNotifiche` | Map of username to personal notification space. | Exposes mutable `utenti` map directly. | Encapsulation and Low Coupling weak. | [recommended] Return unmodifiable map or provide controlled methods. | Repository plus aggregate collection. | recommended |
| `domain/SpazioPersonale.java` - `SpazioPersonale` | Holds notifications for one user. | Duplicate check depends on `Notifica.equals`; no ordering or read/unread concept. | SRP good; Information Expert good. | [optional] Keep; add read status only if required. | Value collection. | optional |
| `domain/Notifica.java` - `Notifica` | Immutable notification record. | Canonical constructor accepts null `id`, breaking documented invariant; uses `LocalDateTime.now()` without app clock. | SRP good; invariant not enforced; DIP weak for time. | [recommended] Validate `id` and inject/provide clock-aware factory for tests. | Factory Method optional. | recommended |
| `domain/PropostaStateChange.java` - `PropostaStateChange` | Immutable state-history entry. | No null checks; `toString` is presentation-ish. | SRP mostly good; minor presentation leakage. | [optional] Validate constructor values; leave formatting to view if needed. | Value Object. | optional |
| `domain/StatoProposta.java` - `StatoProposta` | Proposal state machine transition rules. | Transition rules are embedded in enum; fine now but less flexible if rules need dates/participants. | OCP acceptable now; Polymorphism good. | [optional] Keep enum; move to transition policy only if lifecycle grows. | State-like enum; possible State pattern later. | optional |
| `domain/Proposta.java` - `Proposta` | Proposal entity: fields, values, adherents, state, identity key. | Large entity; has Jackson annotations, parsing, `System.err`, date fallback, state mutation, field map manipulation; generic `Map<String,String>` weakens type safety. | SRP mixed; Information Expert partly good; DIP and MVC boundary weak. | [recommended] Remove console output; extract date/identity helpers and validation policies; consider typed value objects for important fields. | Entity; State pattern optional; Builder/Factory optional. | recommended |
| `domain/CampoBaseDefinito.java` - `CampoBaseDefinito` | Defines fixed base fields from requirements. | Duplicates some names also present in `AppConstants`. | DRY/OCP mild issue; Information Expert good. | [recommended] Make it the single source for fixed field names or generate constants from it. | Enum factory method `toCampo`. | recommended |
| `domain/TipoCampo.java` - `TipoCampo` | Field scope enum. | No issue. | SRP good. | Keep. | None. | optional |
| `domain/TipoDato.java` - `TipoDato` | Field data-type enum. | Adding a new type requires edits in validators and UI. | OCP weak by enum nature, acceptable at this scale. | [optional] Pair enum values with validators/format hints if type set grows. | Strategy per type optional. | optional |
| `domain/TypeValidator.java` - `TypeValidator` | Functional interface for raw value validation. | Return type is nullable string, which is easy to misuse. | ISP and DIP good; Null Object/Result type could improve clarity. | [optional] Return `Optional<String>` or a validation result object. | Strategy. | optional |
| `domain/DefaultTypeValidator.java` - `DefaultTypeValidator` | Default validation for `TipoDato`. | Switch grows with every type; singleton and global date constants reduce flexibility; message text is UI-facing. | OCP and SRP mildly weak. | [recommended] Extract per-type strategies only if more field types/rules appear; separate message text from parse logic. | Strategy; Singleton currently. | recommended |
| `application/AuthenticationService.java` - `AuthenticationService` | Login and registration for both roles. | Plain text credentials; default credentials policy coupled to service; validation duplicated in controller. | SRP mostly okay; security weakness; DIP good for repo. | [recommended] Introduce password hashing and a credential policy; let controller only display validation feedback. | Strategy for password policy optional. | recommended |
| `application/CatalogoService.java` - `CatalogoService` | Use cases for catalog fields and categories. | Duplicate import; thin pass-through in many methods; persistence saved after each mutation. | SRP acceptable; Low Coupling good via repository. | [optional] Remove duplicate import; keep as application boundary; consider transaction/unit-of-work only if multi-step operations grow. | Facade/Application Service. | optional |
| `application/PropostaService.java` - `PropostaService` | Proposal creation, validation, publication, queries, duplicate detection, session-valid proposals. | Too many responsibilities; validation rules, error strings, parsing, duplicate logic, and session state combined. | SRP/OCP/High Cohesion weak; Information Expert split. | [recommended] Extract `ProposalValidator`, `ProposalDuplicateChecker`, and `ProposteValideSessionStore`. | Strategy/Specification; Factory/Builder optional. | recommended |
| `application/StateTransitionService.java` - `StateTransitionService` | Automatic/manual proposal state transitions and notifications. | Builds notification text, parses dates, saves repository, uses concrete `NotificationService`; locking around nested calls may be overbroad. | SRP and DIP weak; Controller/Pure Fabrication role overloaded. | [recommended] Extract transition policies and `NotificationMessageFactory`; depend on notification port interface. | Observer/Domain Events; State policy. | recommended |
| `application/IscrizioneService.java` - `IscrizioneService` | Subscribe/unsubscribe fruitori to proposals. | Duplicates checks already in `Proposta`; uses concrete transition service; calls `LocalDate.now` twice. | SRP okay; DRY and DIP mild issues. | [recommended] Let `Proposta` enforce membership/capacity and service orchestrate persistence/transition; inject clock. | Domain service; Observer event for capacity reached optional. | recommended |
| `application/NotificationService.java` - `NotificationService` | Sends, lists, and deletes notifications. | Depends on repository but no notification port abstraction for transition service; silent no-op on null hides caller errors. | SRP good; DIP okay at persistence boundary. | [optional] Introduce `NotificationPort`; prefer validating nulls consistently. | Facade/Application Service. | optional |
| `application/batch/BatchImportService.java` - `BatchImportService` | Imports JSON data into catalog/proposal flows. | Reads files, maps DTOs, validates types, detects duplicates, builds user-facing errors, and mutates services in one class. | SRP/OCP weak; Low Coupling okay through services. | [recommended] Split into parser, mapper/validator, and import orchestrator if batch grows; reuse proposal validation strategies. | Template pipeline; Strategy validators; Factory for imported proposals. | recommended |
| `application/batch/ImportResult.java` - `ImportResult` | Mutable import counters and error collection. | Error messages are raw strings; no structured error severity/category. | SRP good; OCP limited for richer reporting. | [optional] Introduce `ImportError` record if UI/reporting grows. | Result Object. | optional |
| `application/batch/dto/ImportData.java` - `ImportData` | Root JSON DTO for batch import. | Depends on Jackson annotations, appropriate for DTO. | SRP good. | Keep. | DTO. | optional |
| `application/batch/dto/CampoImportDTO.java` - `CampoImportDTO` | JSON DTO for common field import. | No issue; raw strings require later parsing. | SRP good. | Keep; validation belongs outside DTO. | DTO. | optional |
| `application/batch/dto/CampoSpecificoImportDTO.java` - `CampoSpecificoImportDTO` | JSON DTO for specific field import. | Same structure as `CampoImportDTO`, minor duplication. | SRP good; DRY minor. | [optional] Reuse one DTO only if import schema allows it. | DTO. | optional |
| `application/batch/dto/CategoriaImportDTO.java` - `CategoriaImportDTO` | JSON DTO for category import. | No issue. | SRP good. | Keep. | DTO. | optional |
| `application/batch/dto/PropostaImportDTO.java` - `PropostaImportDTO` | JSON DTO for proposal import. | `Map<String,String>` mirrors weakly typed proposal values. | SRP good; type safety weak by design. | [optional] Introduce typed import value objects if proposal schema stabilizes. | DTO; Builder input. | optional |
| `persistence/api/ICatalogoRepository.java` - `ICatalogoRepository` | Catalog persistence abstraction. | Interface name uses `I` prefix; `get` returns live aggregate and `save` persists implicit cached state. | DIP good; ISP fine; implicit Unit of Work can surprise. | [optional] Rename to `CatalogoRepository`; consider `save(Catalogo)` for clarity. | Repository. | optional |
| `persistence/api/IBachecaRepository.java` - `IBachecaRepository` | Bacheca persistence abstraction. | Same implicit cached-state pattern. | DIP good; ISP fine. | [optional] Rename and make save explicit if repositories become more complex. | Repository. | optional |
| `persistence/api/ICredenzialiRepository.java` - `ICredenzialiRepository` | Credentials persistence abstraction. | Same naming/cached-state issue. | DIP good. | [optional] Rename; consider separate account repository if auth model grows. | Repository. | optional |
| `persistence/api/ISpazioPersonaleRepository.java` - `ISpazioPersonaleRepository` | Personal notification persistence abstraction. | `get(username)` creates if absent, so query has side effect. | Command-query separation weak; Information Expert acceptable. | [recommended] Rename to `getOrCreate` or split query/create behavior. | Repository. | recommended |
| `persistence/impl/AbstractFileRepository.java` - `AbstractFileRepository` | Generic JSON file load/save with mapper and atomic write. | Logs to `System.err`; catches read errors and silently starts empty; date formats duplicated with `AppConstants`; static mapper limits configuration. | SRP mostly good; error policy and serialization mixed; DIP weak for logging. | [recommended] Inject/centralize serialization config and error policy; avoid silent data loss by surfacing corrupted file errors. | Template Method/Abstract superclass. | recommended |
| `persistence/impl/FileCatalogoRepository.java` - `FileCatalogoRepository` | JSON repository for catalog with cache. | Boilerplate duplicated across file repositories; cache is not synchronized. | SRP good; DRY minor. | [optional] Generic cached repository wrapper could remove duplication. | Repository Adapter. | optional |
| `persistence/impl/FileBachecaRepository.java` - `FileBachecaRepository` | JSON repository for bacheca with cache. | Same duplication and unsynchronized cache. | SRP good. | [optional] Same generic cached repository wrapper. | Repository Adapter. | optional |
| `persistence/impl/FileCredenzialiRepository.java` - `FileCredenzialiRepository` | JSON repository for credentials with cache. | Same duplication; stores plain text credentials from domain. | SRP good but security model weak upstream. | [recommended] Pair with password hashing change. | Repository Adapter. | recommended |
| `persistence/impl/FileSpazioPersonaleRepository.java` - `FileSpazioPersonaleRepository` | JSON repository for notification archive. | Class is not `final` unlike peers; `get` creates records implicitly. | Consistency issue; command-query separation weak. | [recommended] Make final for consistency unless extension is intended; rename behavior to `getOrCreate`. | Repository Adapter. | recommended |
| `presentation/view/contract/IOutputView.java` - `IOutputView` | Output API for console-like UI. | Large and domain-specific; view interface knows domain classes directly. | ISP intent good but interface still broad; Low Coupling weak. | [recommended] Split generic console output from domain presentation or introduce view models. | Presenter/View Model optional. | recommended |
| `presentation/view/contract/IInputView.java` - `IInputView` | Input API and selection/form operations. | Handles generic input plus domain-specific proposal forms and validators. | ISP/SRP weak despite split from output. | [recommended] Extract proposal form interface from basic input interface. | Strategy for field validation; Form Object. | recommended |
| `presentation/view/contract/IAppView.java` - `IAppView` | Composite input/output view for controllers. | Encourages controllers to depend on all UI methods; duplicate cancel hint with `ConsoleUI`. | ISP weakened by composite dependency. | [optional] Inject smaller interfaces per controller. | Facade for CLI view. | optional |
| `presentation/view/contract/ProposalFieldValidator.java` - `ProposalFieldValidator` | Callback for field-level business validation during forms. | Depends on domain `Proposta` and mutable-value map shape. | DIP useful; coupling to domain form model. | [optional] Use a small form context DTO if form logic grows. | Strategy. | optional |
| `presentation/view/contract/OperationCancelledException.java` - `OperationCancelledException` | Signals user cancellation from view/input. | Exception as control flow is acceptable in CLI but can spread. | SRP good; Controller flow coupling mild. | [optional] Return cancellation results in form APIs; keep exception for simple input. | Result Object alternative. | optional |
| `presentation/view/cli/ConsoleUI.java` - `ConsoleUI` | Concrete console rendering, input parsing, selection, and proposal form handling. | Very large; mixes UI rendering, input loops, type validation, proposal form workflow, and domain-specific formatting. | SRP/High Cohesion weak; MVC View too smart. | [recommended] Split into `ConsolePrinter`, `ConsoleInput`, `MenuRenderer`, and `ProposalFormView` or presenter. | Adapter; Strategy for validators; Presenter optional. | recommended |
| `presentation/controller/AuthController.java` - `AuthController` | Access menu, login/registration, and session delegation. | Now depends on `AuthView` and services/controllers for orchestration only. | SRP improved; UI moved to feature view. | [optional] Keep watching top-level session scope as flows grow. | Controller; Strategy for credential policy optional. | optional |
| `presentation/controller/ConfiguratoreController.java` - `ConfiguratoreController` | Configurator catalog, proposal, withdrawal, archive, and batch-import orchestration. | Broad but pure: no UI strings and no direct view implementation dependency. | Controller role clear; feature breadth remains. | [optional] Keep view/service split stable before any further decomposition. | Command for menu actions optional. | optional |
| `presentation/controller/FruitoreController.java` - `FruitoreController` | Fruitore bacheca, subscription, unsubscription, and personal notifications. | Now depends on `FruitoreView` and services for orchestration only. | SRP improved; formatting moved to view. | [optional] Add dedicated view-models only if fruitore screens grow. | Presenter optional. | optional |

## Priority List of Recommended Refactorings

1. [recommended] Split `Application` into dependency wiring, top-level menu/session control, and midnight scheduler. This reduces the biggest architectural mixing with minimal domain risk.
2. [recommended] Extract proposal validation from `PropostaService` into dedicated rule objects. Start with mandatory fields, participant count, date constraints, and duplicate identity.
3. [recommended] Move notification message construction out of `StateTransitionService`. Use a `NotificationMessageFactory` or domain events handled by a notification listener.
4. [recommended] Reduce `ConsoleUI` by extracting proposal-form handling and basic input/rendering helpers. This will make controllers and views easier to test.
5. [recommended] Fix concrete invariant/encapsulation issues: mutable copy in `Categoria`, global-name check in `Catalogo`, exposed mutable map in `ArchivioNotifiche`, unenforced APERTA rule in `Bacheca`, null `Notifica.id`.
6. [recommended] Improve time and persistence boundaries: inject `Clock`, avoid `System.err` in domain/persistence, and decide whether Jackson annotations belong in domain or DTO mappers.
7. [recommended] Add focused tests for proposal validation, state transitions, subscription/disiscription rules, batch import edge cases, and repositories with temporary files.

## Final Notes: What To Improve First

Start with the low-risk correctness and cohesion improvements:

- Fix domain invariants and encapsulation first. These are small changes with high reliability value.
- Then extract proposal validation. It is the clearest design pressure point and will simplify both interactive creation and batch import.
- Then split `Application` and `ConsoleUI`. These changes improve architecture without changing business rules.
- Delay bigger pattern work. Use Strategy and small factories where they reduce existing complexity; avoid a full State pattern or event architecture until the lifecycle and notification rules actually grow.

The project does not need a full rewrite. It needs a few deliberate extra boundaries so each class has one stronger reason to exist.
