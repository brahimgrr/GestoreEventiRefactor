# Proposta Refactoring Plan

## Summary

Refactor the `Proposta` area to reduce SRP/OCP/GRASP violations while preserving current CLI behavior, JSON compatibility, lifecycle behavior, duplicate detection, and notification behavior.

Chosen approach: incremental extraction. Keep `Proposta` as the domain aggregate, but move identity, validation rules, parsing, and persistence lookup into focused collaborators. Avoid a full State-pattern rewrite for now because `StatoProposta` is already compact and readable.

## Target Responsibilities

- `Proposta`: domain aggregate only. Owns fields, state, state history, subscriptions, publication/withdrawal/conclusion mutations, and invariant checks.
- `PropostaValidationService`: application entry point for validating and applying values.
- `DefaultPropostaValidationPolicy`: domain validation coordinator.
- `PropostaValidationRule`: Strategy interface for individual validation rules.
- `PropostaIdentityPolicy`: single source of truth for duplicate/identity key generation.
- `PropostaLookupService`: application helper for finding the persisted version of a selected proposal.
- `PropostaLifecycleService`: coordinates lifecycle use cases, but does not duplicate lookup or identity logic.

## Key Changes

- Extract validation from `Proposta.valida()` and `Proposta.validaCampo(...)`.
  - Create validation rules: required fields, participant count, subscription deadline, event date, conclusion date.
  - Replace hard-coded switch logic with rule strategies that decide whether they apply to full validation or single-field validation.
  - `PropostaValidationService` becomes the only production caller for validation.

- Slim `Proposta`.
  - Remove direct parsing/validation orchestration from the aggregate.
  - Keep methods like `pubblica`, `confermaSeAperta`, `annullaSeAperta`, `concludiSeConfermata`, `ritira`, `iscrivi`, `disiscrivi`.
  - Add narrowly named package-level methods for validation outcomes, for example `riportaInBozzaSeValida()` and `segnaValidata(termineIscrizione, dataEvento)`, so state changes still pass through the aggregate.

- Extract identity policy.
  - Move current `Proposta.chiaveIdentita(Map<String, String>)` logic into `PropostaIdentityPolicy`.
  - Update `PropostaPublicationService`, `Bacheca`, `BatchImportService`, `IscrizioneService`, and `PropostaLifecycleService` to use the policy.
  - Keep `Proposta.getChiaveIdentita()` temporarily as a delegating compatibility method if tests/controllers still need it.

- Remove duplicated persisted-proposal lookup.
  - Create `PropostaLookupService` with `trovaInBacheca(Bacheca, Proposta)`.
  - Use it in `PropostaLifecycleService` and `IscrizioneService`.
  - Throw the same `DomainException(PROPOSTA_NON_TROVATA, chiave)` as today.

- Keep lifecycle state simple.
  - Keep `StatoProposta.canTransitionTo` as the current lightweight State-pattern implementation.
  - Do not introduce separate state classes unless lifecycle rules grow beyond the current enum behavior.

## SOLID / GRASP Mapping

- SRP: `Proposta` no longer validates form fields, parses dates, builds identity keys, and manages state all in one class.
- OCP: new proposal validation rules are added as new `PropostaValidationRule` implementations, not by editing a switch in `Proposta`.
- DIP: application services depend on validation/identity interfaces or focused collaborators, not static helper methods.
- GRASP Information Expert: `Proposta` remains expert for its own state and subscription invariants.
- GRASP Pure Fabrication: validation policy, identity policy, and lookup service exist to reduce coupling and improve cohesion.
- GRASP Low Coupling / High Cohesion: publication, lifecycle, validation, and lookup each have one reason to change.

## GoF Pattern Applications

- Strategy: `PropostaValidationRule` for each validation rule.
- Composite-like coordinator: `DefaultPropostaValidationPolicy` runs a list of rules and merges errors.
- State: preserve current enum-based State pattern in `StatoProposta`.
- Facade: keep `PropostaService` as a compatibility facade over creation, validation, publication, lifecycle, and query services.
- Factory: keep `PropostaFactory`; no need to expand it unless construction variants grow.

## Implementation Sequence

1. Add characterization tests around current `Proposta` behavior:
   - valid proposal becomes `VALIDA`;
   - invalid proposal stays or returns to `BOZZA`;
   - required-field errors are unchanged;
   - participant number errors are unchanged;
   - cross-field date errors are unchanged;
   - single-field validation returns the same errors as today.

2. Add `PropostaIdentityPolicy`.
   - Move identity-key logic unchanged.
   - Update duplicate detection and lookup usage.
   - Keep tests for duplicate proposal detection in saved proposals, bacheca, and batch import.

3. Add validation rule model.
   - Create `PropostaValidationRule`.
   - Create `PropostaValidationContext` containing proposal, current values, parsed dates, field-under-validation if any, and current date.
   - Create `PropostaFieldParser` for date and participant parsing.
   - Create rule classes for the current checks.

4. Rewrite `PropostaValidationService`.
   - `validaProposta(proposta)` calls the policy.
   - If no errors, service asks `Proposta` to store parsed dates and transition to `VALIDA`.
   - If errors exist, service asks `Proposta` to return from `VALIDA` to `BOZZA` when needed.
   - `validaCampo(...)` uses the same rules with a single-field context.

5. Remove validation internals from `Proposta`.
   - Delete or reduce `parseData`, `controllaCampiObbligatori`, `controllaNumeroPartecipanti`, and the switch in `validaCampo`.
   - Keep only aggregate mutation and invariant methods.
   - Update tests and production callers to go through `PropostaValidationService`.

6. Extract `PropostaLookupService`.
   - Replace duplicated `trovaPropostaPersistita` in lifecycle and subscription flows.
   - Preserve the existing behavior where selected query objects are resolved back to the persisted object before mutation.

7. Run full verification.
   - Run `mvn test`.
   - Add targeted tests until validation, lookup, lifecycle confirmation, subscription persistence, and duplicate detection are covered.

## Test Plan

- Unit tests for each validation rule.
- Service tests for `PropostaValidationService.applicaValoriEValida`.
- Identity policy tests for title/date/time/place normalization.
- Lookup tests for null proposal, missing proposal, and found persisted proposal.
- Regression tests for `IscrizioneService` confirming a proposal at capacity.
- Regression tests for lifecycle confirmation, annulment, withdrawal, and conclusion.
- Full command: `mvn test`.

## Assumptions

- No CLI behavior changes.
- No JSON schema changes.
- Existing error codes and messages remain unchanged.
- This refactor stays inside the proposal domain/application area; broader view/controller cleanup is separate.
