# Testing Summary

## Implemented Test Suite

The project now has a Maven/JUnit 5 test setup:

- `mvn test` is configured for unit tests named `*Test`.
- `mvn verify` is configured for unit tests plus integration tests named `*IT`.
- Production sources remain in `src`.
- Unit tests are in `unit_tests`.
- Integration tests are in `integration_tests`.

Note: this workspace currently does not have `mvn` installed on `PATH`, so the suite was implemented but could not be executed locally from this shell.

## Covered Use Cases

| Use cases | Coverage |
|---|---|
| UC1-UC2 | Authentication and registration unit tests plus controller/service/file-repository integration. |
| UC3-UC12 | Catalog, category, common field, and specific field service tests plus file-backed catalog integration. |
| Included field use cases | Covered through add/remove/mandatory field service behavior and validation tests. |
| UC13-UC15 | Proposal validation, publication, duplicate detection, and bacheca grouping tests. |
| UC16, UC21 | Subscription and unsubscription behavior, duplicate subscription, expired deadline, and persistence. |
| UC17-UC18 | Notification listing/deletion through service and controller flow. |
| UC20 | Proposal withdrawal, invalid withdrawal timing/state, notifications, and persistence. |
| UC22 | Batch import success, best-effort invalid element handling, missing file, and empty import. |

## Missing Or Partially Covered Cases

- UC19 is partially covered. The manual says the archive should navigate first by category and show proposal state/history. Current `PropostaController.visualizzaArchivioProposte()` navigates first by state. `UC19_ArchivioProposteIT` is intentionally disabled to document this mismatch instead of hiding it.
- Full menu-loop coverage for `ConfiguratoreController` is intentionally limited. The class has large private loops and heavy interactive flow. The tests cover the underlying use-case behavior through services and selected smaller controllers.
- UI rendering details are not exhaustively asserted. The tests focus on documented behavior and state changes, not exact console formatting.

## Design Issues Discovered During Testing

- The repo had no existing build or test framework; `pom.xml` was added.
- The project uses records and `Stream.toList()`, so Java 17 is required even though the local shell reports Java 8.
- `AppConstants.clock` is a global mutable clock. Tests can control it, but dependency injection would be cleaner.
- Controllers and `ConsoleUI` are difficult to test deeply because input loops, rendering, and business-oriented form workflows are tightly coupled.
- UC19 behavior differs from the manual: current archive navigation is state-first, not category-first.
- `.gitignore` ignored all root artifacts by default, so exceptions were added for Maven, docs, and test directories.

## Suggested Improvements

- Extract menu actions and proposal forms from large controllers into smaller collaborators.
- Inject `Clock` into services and entities that need current dates.
- Add presenters or view models for bacheca/archive/spazio personale displays.
- Align UC19 implementation with the manual by grouping archive navigation by category, then showing proposal state and state history.
- Consider moving JSON annotations out of domain classes if persistence formats keep growing.

## How To Run

After installing Maven and using JDK 17 or newer:

```powershell
mvn test
mvn verify
```
