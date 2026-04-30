# Use Case Test Mapping

Primary reference: `Manuale d'uso.md`.

The test names intentionally start with the use-case identifiers where practical, for example `UC14_pubblicaProposta_success_setsAPERTAAndPersistsInBacheca`.

## Extracted Use Cases

| Use case | Actors | Preconditions | Main flow summary / expected outcome | Important alternatives/failures |
|---|---|---|---|---|
| UC1 Login | Configuratore, Fruitore | Actor can access login screen. | Valid credentials authenticate actor and route to the correct main menu. | Default configuratore credentials extend UC2; invalid credentials retry; missing base fields extend UC3. |
| UC2 Registrazione | Configuratore, Fruitore | Configuratore first login or fruitore chooses registration. | New credentials are validated and saved persistently. | Cancel, duplicate username, invalid username/password, confirmation denied. |
| UC3 Fissare Campi Base | Configuratore | Configuratore logged in and base fields not fixed. | Eight fixed base fields plus optional extra fields are fixed and persisted. | No extras, cancel extra field, duplicate/fixed name, final confirmation denied. |
| UC4 Visualizzare Campi e Categorie | Configuratore | Configuratore logged in. | Catalog data is shown without state mutation. | None documented. |
| UC5 Aggiungi Campo Comune | Configuratore | Configuratore logged in. | New common field is added and catalog persisted. | Included Aggiungi Campo validation failures. |
| UC6 Rimuovi Campo Comune | Configuratore | Configuratore logged in. | Selected common field is removed and catalog persisted. | No common fields, cancel, confirmation denied. |
| UC7 Modifica Obbligatorieta Campo Comune | Configuratore | Configuratore logged in. | Mandatory flag is updated and catalog persisted. | No common fields, cancel, confirmation denied. |
| UC8 Crea Categoria | Configuratore | Configuratore logged in. | New empty category is created and persisted. | Cancel, duplicate category name. |
| UC9 Rimuovi Categoria | Configuratore | Configuratore logged in. | Category and specific fields are removed and persisted. | No categories, cancel, confirmation denied. |
| UC10 Aggiungi Campo Specifico | Configuratore | Configuratore logged in. | Specific field is added to selected category and persisted. | No categories, cancel, included field validation failures. |
| UC11 Rimuovi Campo Specifico | Configuratore | Configuratore logged in. | Specific field is removed and catalog persisted. | No categories, cancel, no specific fields. |
| UC12 Modifica Obbligatorieta Campo Specifico | Configuratore | Configuratore logged in. | Specific field mandatory flag is updated and persisted. | No categories, cancel, no fields. |
| Included: Aggiungi Campo | Configuratore | Called by a field-add use case. | Field name/type/mandatory flag are acquired. | Cancel, blank/duplicate name, invalid type, confirmation denied. |
| Included: Rimuovi Campo | Configuratore | At least one field selectable. | Selected field is marked for removal. | Cancel, confirmation denied. |
| Included: Modifica Obbligatorieta Campo | Configuratore | At least one field selectable. | New mandatory flag is acquired. | Cancel, confirmation denied. |
| UC13 Creare Proposta | Configuratore | Configuratore logged in. | Proposal fields are validated incrementally; valid proposal becomes `VALIDA`. | No categories, cancel, invalid field value/date rules. |
| UC14 Pubblicare Proposta | Configuratore | Configuratore logged in. | `VALIDA` proposal becomes `APERTA`, gets publication date, is persisted in bacheca. | No valid proposals, cancel, confirmation denied, expired deadline, duplicate identity. |
| UC15 Visualizzare Bacheca | Configuratore, Fruitore | Actor logged in. | Open proposals are shown grouped by category with field values. | Empty bacheca. |
| UC16 Iscriversi a Proposta Aperta | Fruitore | Fruitore logged in, proposal open. | Fruitore is added to adherents and proposal is persisted. | Already subscribed, no confirmation, expired/full/non-open proposal. |
| UC17 Visualizzare Spazio Personale | Fruitore | Fruitore logged in. | Personal notifications are shown. | No notifications; extends UC18 for deletion. |
| UC18 Eliminare Notifica | Fruitore | Fruitore opened personal space and has notifications. | Selected notification is removed and persisted. | Confirmation denied. |
| UC19 Visualizzare Archivio Proposte | Configuratore | Configuratore logged in. | Manual expects archived proposals navigated by category with state history. | Empty archive, cancel. Current implementation navigates by state first. |
| UC20 Ritirare Proposta | Configuratore | Configuratore logged in. | Open/confirmed proposal before event date becomes `RITIRATA`; adherents are notified; state is persisted. | No withdrawable proposals, cancel, no confirmation, withdrawal deadline passed. |
| UC21 Disdire Iscrizione | Fruitore | Fruitore logged in. | Fruitore is removed from open proposal adherents and change is persisted. | No active subscriptions, cancel, no confirmation, expired subscription deadline. |
| UC22 Importare Dati Batch | Configuratore | Configuratore logged in. | Valid JSON elements are imported best-effort; valid proposals become publishable. | Missing/unreadable file, invalid element skipped, empty import. |

## Traceability Table

| Use case | Main classes/methods | Unit tests | Integration tests |
|---|---|---|---|
| UC1 Login | `AuthController.loginConfiguratore`, `AuthController.loginFruitore`, `AuthenticationService.login`, `AuthenticationService.loginFruitore` | `AuthenticationServiceTest` | `UC01_UC02_AuthenticationIT` |
| UC2 Registrazione | `AuthController.registraFruitore`, `AuthenticationService.registraNuovoConfiguratore`, `AuthenticationService.registraNuovoFruitore`, `Credenziali.addConfiguratore`, `Credenziali.addFruitore` | `AuthenticationServiceTest` | `UC01_UC02_AuthenticationIT` |
| UC3 Fissare Campi Base | `CatalogoService.initiateCampiBase`, `CatalogoService.addCampiBaseConExtra`, `Catalogo.fissareCampiBase` | `CatalogoServiceTest` | `UC03_UC12_CatalogoManagementIT` |
| UC4 Visualizzare Campi e Categorie | `CatalogoService.getCampiBase`, `getCampiComuni`, `getCategorie`, `ConsoleUI.stampaCampi`, `stampaCategorie` | `CatalogoServiceTest` | `UC03_UC12_CatalogoManagementIT` |
| UC5 Aggiungi Campo Comune | `CatalogoService.addCampoComune`, `Catalogo.addCampoComune`, `Campo` | `CatalogoServiceTest` | `UC03_UC12_CatalogoManagementIT` |
| UC6 Rimuovi Campo Comune | `CatalogoService.removeCampoComune`, `Catalogo.removeCampoComune` | `CatalogoServiceTest` | `UC03_UC12_CatalogoManagementIT` |
| UC7 Modifica Obbligatorieta Campo Comune | `CatalogoService.setObbligatorietaCampoComune`, `Catalogo.updateCampoComune` | `CatalogoServiceTest` | `UC03_UC12_CatalogoManagementIT` |
| UC8 Crea Categoria | `CatalogoService.createCategoria`, `Catalogo.addCategoria`, `Categoria` | `CatalogoServiceTest` | `UC03_UC12_CatalogoManagementIT` |
| UC9 Rimuovi Categoria | `CatalogoService.removeCategoria`, `Catalogo.removeCategoria` | `CatalogoServiceTest` | `UC03_UC12_CatalogoManagementIT` |
| UC10 Aggiungi Campo Specifico | `CatalogoService.addCampoSpecifico`, `Catalogo.addCampoSpecifico`, `Categoria.addCampoSpecifico` | `CatalogoServiceTest` | `UC03_UC12_CatalogoManagementIT` |
| UC11 Rimuovi Campo Specifico | `CatalogoService.removeCampoSpecifico`, `Categoria.removeCampoSpecifico` | `CatalogoServiceTest` | `UC03_UC12_CatalogoManagementIT` |
| UC12 Modifica Obbligatorieta Campo Specifico | `CatalogoService.setObbligatorietaCampoSpecifico`, `Categoria.setObbligatorietaCampoSpecifico` | `CatalogoServiceTest` | `UC03_UC12_CatalogoManagementIT` |
| Included: Aggiungi Campo | `ConsoleUI.acquisisciTipoDato`, `CatalogoService.addCampoComune`, `addCampoSpecifico`, `Campo` | `CatalogoServiceTest` | `UC03_UC12_CatalogoManagementIT` |
| Included: Rimuovi Campo | `IInputView.selezionaElemento`, service removal methods | `CatalogoServiceTest` | `UC03_UC12_CatalogoManagementIT` |
| Included: Modifica Obbligatorieta Campo | service mandatory update methods | `CatalogoServiceTest` | `UC03_UC12_CatalogoManagementIT` |
| UC13 Creare Proposta | `ConfiguratoreController`, `PropostaService.creaProposta`, `validaCampo`, `validaProposta`, `DefaultTypeValidator`, `Proposta.putAllValoriCampi` | `PropostaServiceTest`, `PropostaDomainTest` | `UC13_UC15_ProposalPublicationIT` |
| UC14 Pubblicare Proposta | `ConfiguratoreController`, `PropostaService.salvaProposta`, `pubblicaProposta`, `Bacheca.addProposta` | `PropostaServiceTest`, `PropostaDomainTest` | `UC13_UC15_ProposalPublicationIT` |
| UC15 Visualizzare Bacheca | `PropostaService.getBacheca`, `getBachecaPerCategoria`, `ConsoleUI.mostraBacheca` | `PropostaServiceTest` | `UC13_UC15_ProposalPublicationIT` |
| UC16 Iscriversi | `FruitoreController`, `IscrizioneService.iscrivi`, `Proposta.addAderente`, `StateTransitionService.confermaProposta` | `IscrizioneServiceTest`, `PropostaDomainTest` | `UC16_UC21_FruitoreSubscriptionIT` |
| UC17 Visualizzare Spazio Personale | `FruitoreController`, `NotificationService.getNotifiche`, `SpazioPersonale.getNotifiche` | `NotificationServiceTest` | `UC17_UC18_SpazioPersonaleIT` |
| UC18 Eliminare Notifica | `FruitoreController`, `NotificationService.cancellaNotifica`, `SpazioPersonale.removeNotifica` | `NotificationServiceTest` | `UC17_UC18_SpazioPersonaleIT` |
| UC19 Visualizzare Archivio Proposte | `ConfiguratoreController`, `PropostaService.getPropostePerStato`, `Proposta.getStateHistory` | Covered indirectly by proposal state tests | `UC19_ArchivioProposteIT` disabled to document manual/code mismatch |
| UC20 Ritirare Proposta | `ConfiguratoreController.ritiraProposta`, `StateTransitionService.ritiraProposta`, `NotificationService.inviaNotifica` | `StateTransitionServiceTest` | `UC20_RitiroPropostaIT` |
| UC21 Disdire Iscrizione | `FruitoreController.disdiciIscrizione`, `IscrizioneService.disiscrivi`, `Proposta.removeAderente` | `IscrizioneServiceTest`, `PropostaDomainTest` | `UC16_UC21_FruitoreSubscriptionIT` |
| UC22 Import Batch | `ConfiguratoreController`, `BatchImportService.importa`, import DTOs, catalog/proposal services | `BatchImportServiceTest` | `UC22_BatchImportIT` |

## Test Organization

| Directory | Purpose |
|---|---|
| `unit_tests/` | Isolated tests for services, domain rules, import logic, and notification behavior. |
| `integration_tests/` | Tests that exercise real collaboration between controllers/services/domain/repositories, using temporary files for persistence. |
| `unit_tests/it/unibs/ingsoft/testsupport/` | In-memory repositories, fixed-clock fixtures, and scripted view test helper. |
