package it.unibs.ingsoft.application.batch;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unibs.ingsoft.application.CatalogoService;
import it.unibs.ingsoft.application.PropostaService;
import it.unibs.ingsoft.application.batch.dto.*;
import it.unibs.ingsoft.domain.*;
import it.unibs.ingsoft.v5.application.batch.dto.*;
import it.unibs.ingsoft.v5.domain.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Gestisce l'importazione batch di campi comuni, categorie e proposte da un file JSON.
 *
 * <p>L'ordine di elaborazione rispetta la catena di dipendenze:
 * <ol>
 *   <li>Campi comuni</li>
 *   <li>Categorie con i relativi campi specifici</li>
 *   <li>Proposte (dipendono da categorie e campi)</li>
 * </ol>
 *
 * <p>Strategia <em>best-effort</em>: ogni entità è elaborata indipendentemente;
 * le voci non valide vengono saltate con un messaggio di errore.
 */
public final class BatchImportService {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final CatalogoService catalogoService;
    private final PropostaService propostaService;

    public BatchImportService(CatalogoService catalogoService, PropostaService propostaService) {
        this.catalogoService = Objects.requireNonNull(catalogoService);
        this.propostaService = Objects.requireNonNull(propostaService);
    }

    private static TipoDato parseTipoDato(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return TipoDato.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Importa le entità dal file JSON indicato.
     *
     * @param filePath percorso del file JSON di importazione
     * @return esito con contatori di successo ed eventuali errori
     * @throws IOException se il file non esiste, non è leggibile o non è JSON valido
     */
    public ImportResult importa(Path filePath) throws IOException {
        if (!Files.exists(filePath))
            throw new IOException("File non trovato: " + filePath);
        if (!Files.isReadable(filePath))
            throw new IOException("File non leggibile: " + filePath);

        ImportData data = MAPPER.readValue(filePath.toFile(), ImportData.class);
        ImportResult result = new ImportResult();

        importaCampiComuni(data.campiComuni(), result);
        importaCategorie(data.categorie(), result);
        importaProposte(data.proposte(), result);

        return result;
    }

    private void importaCampiComuni(List<CampoImportDTO> campi, ImportResult result) {
        Set<String> nomiVisti = new HashSet<>();

        for (CampoImportDTO dto : campi) {
            String nome = dto.nome();

            if (nome == null || nome.isBlank()) {
                result.addErrore("[Campo comune] nome vuoto o mancante — campo ignorato.");
                continue;
            }

            if (!nomiVisti.add(nome.toLowerCase())) {
                result.addErrore("[Campo comune] '" + nome + "': duplicato nel file di importazione.");
                continue;
            }

            TipoDato tipoDato = parseTipoDato(dto.tipoDato());
            if (tipoDato == null) {
                result.addErrore("[Campo comune] '" + nome + "': tipoDato non valido: \"" + dto.tipoDato() + "\".");
                continue;
            }

            try {
                catalogoService.addCampoComune(nome, tipoDato, dto.obbligatorio());
                result.incrementCampiComuni();
            } catch (IllegalArgumentException e) {
                result.addErrore("[Campo comune] '" + nome + "': " + e.getMessage());
            }
        }
    }

    private void importaCategorie(List<CategoriaImportDTO> categorie, ImportResult result) {
        Set<String> nomiVisti = new HashSet<>();

        for (CategoriaImportDTO dto : categorie) {
            String nome = dto.nome();

            if (nome == null || nome.isBlank()) {
                result.addErrore("[Categoria] nome vuoto o mancante — categoria ignorata.");
                continue;
            }

            if (!nomiVisti.add(nome.toLowerCase())) {
                result.addErrore("[Categoria] '" + nome + "': duplicata nel file di importazione.");
                continue;
            }

            try {
                catalogoService.createCategoria(nome);
            } catch (IllegalArgumentException e) {
                result.addErrore("[Categoria] '" + nome + "': " + e.getMessage());
                continue;
            }

            for (CampoSpecificoImportDTO campoDTO : dto.campiSpecifici()) {
                String nomeCampo = campoDTO.nome();

                if (nomeCampo == null || nomeCampo.isBlank()) {
                    result.addErrore("[Campo specifico] in categoria '" + nome + "': nome vuoto — campo ignorato.");
                    continue;
                }

                TipoDato tipoDato = parseTipoDato(campoDTO.tipoDato());
                if (tipoDato == null) {
                    result.addErrore("[Campo specifico] '" + nomeCampo + "' in categoria '" + nome
                            + "': tipoDato non valido: \"" + campoDTO.tipoDato() + "\".");
                    continue;
                }

                try {
                    catalogoService.addCampoSpecifico(nome, nomeCampo, tipoDato, campoDTO.obbligatorio());
                } catch (IllegalArgumentException e) {
                    result.addErrore("[Campo specifico] '" + nomeCampo + "' in categoria '" + nome + "': " + e.getMessage());
                }
            }

            // La categoria è contata come importata anche se alcuni campi specifici falliscono.
            result.incrementCategorie();
        }
    }

    private void importaProposte(List<PropostaImportDTO> proposte, ImportResult result) {
        Set<String> chiaviBatch = new HashSet<>();

        for (PropostaImportDTO dto : proposte) {
            String nomeCategoria = dto.categoria();
            Map<String, String> valori = dto.valoriCampi();
            String titolo = valori.getOrDefault(AppConstants.CAMPO_TITOLO, "(senza titolo)");

            if (nomeCategoria == null || nomeCategoria.isBlank()) {
                result.addErrore("[Proposta] '" + titolo + "': nome categoria mancante.");
                continue;
            }

            Categoria categoria = trovaCategoriaPerNome(nomeCategoria);
            if (categoria == null) {
                result.addErrore("[Proposta] '" + titolo + "': categoria '" + nomeCategoria + "' non trovata nel catalogo.");
                continue;
            }

            // Duplicati intra-file (Titolo + Data + Ora + Luogo, case-insensitive).
            // Duplicati cross-source (bacheca + proposteValide) sono rilevati da salvaProposta().
            String chiave = Proposta.chiaveIdentita(valori);
            if (!chiaviBatch.add(chiave)) {
                result.addErrore("[Proposta] '" + titolo + "': duplicata nel file di importazione (stesso Titolo, Data, Ora, Luogo).");
                continue;
            }

            List<Campo> campiBase = catalogoService.getCampiBase();
            List<Campo> campiComuni = catalogoService.getCampiComuni();

            List<Campo> tuttiCampi = new ArrayList<>();
            tuttiCampi.addAll(campiBase);
            tuttiCampi.addAll(campiComuni);
            tuttiCampi.addAll(categoria.getCampiSpecifici());

            List<String> erroriTipo = new ArrayList<>();

            for (Campo campo : tuttiCampi) {
                String valore = valori.get(campo.getNome());
                if (valore != null && !valore.isBlank()) {
                    String errore = DefaultTypeValidator.INSTANCE.validate(valore, campo.getTipoDato());
                    if (errore != null) {
                        erroriTipo.add("campo \"" + campo.getNome() + "\": " + errore);
                    }
                }
            }

            if (!erroriTipo.isEmpty()) {
                for (String e : erroriTipo)
                    result.addErrore("[Proposta] '" + titolo + "': " + e);
                continue;
            }

            try {
                Proposta proposta = propostaService.creaProposta(categoria, campiBase, campiComuni);
                proposta.putAllValoriCampi(valori);

                List<String> erroriValidazione = propostaService.validaProposta(proposta);
                if (!erroriValidazione.isEmpty()) {
                    for (String e : erroriValidazione)
                        result.addErrore("[Proposta] '" + titolo + "': " + e);
                    continue;
                }

                propostaService.salvaProposta(proposta);
                result.incrementProposte();

            } catch (IllegalArgumentException | IllegalStateException e) {
                result.addErrore("[Proposta] '" + titolo + "': " + e.getMessage());
            }
        }
    }

    private Categoria trovaCategoriaPerNome(String nome) {
        return catalogoService.getCategorie().stream()
                .filter(c -> c.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .orElse(null);
    }
}
