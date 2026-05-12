package it.unibs.ingsoft.application.batch;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unibs.ingsoft.application.batch.dto.CampoImportDTO;
import it.unibs.ingsoft.application.batch.dto.CampoSpecificoImportDTO;
import it.unibs.ingsoft.application.batch.dto.CategoriaImportDTO;
import it.unibs.ingsoft.application.batch.dto.ImportData;
import it.unibs.ingsoft.application.batch.dto.ImportResult;
import it.unibs.ingsoft.application.batch.dto.PropostaImportDTO;
import it.unibs.ingsoft.application.catalogo.Catalogo_Service;
import it.unibs.ingsoft.application.proposta.Proposta_Service;
import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.Campo;
import it.unibs.ingsoft.domain.Categoria;
import it.unibs.ingsoft.domain.DefaultTypeValidator;
import it.unibs.ingsoft.domain.Proposta;
import it.unibs.ingsoft.domain.TipoDato;
import it.unibs.ingsoft.domain.error.DomainErrorCode;
import it.unibs.ingsoft.domain.error.DomainException;
import it.unibs.ingsoft.domain.error.ImportError;
import it.unibs.ingsoft.domain.error.ValidationError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Gestisce l'importazione batch di campi comuni, categorie e proposte da un file JSON.
 */
public final class BatchImportService {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final Catalogo_Service catalogoService;
    private final Proposta_Service propostaService;

    public BatchImportService(Catalogo_Service catalogoService, Proposta_Service propostaService) {
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

    public ImportResult importa(Path filePath) throws IOException {
        if (!Files.exists(filePath))
            throw new DomainException(DomainErrorCode.IMPORT_FILE_NON_TROVATO, filePath);
        if (!Files.isReadable(filePath))
            throw new DomainException(DomainErrorCode.IMPORT_FILE_NON_LEGGIBILE, filePath);

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
                result.addErrore(ImportError.of(DomainErrorCode.IMPORT_CAMPO_COMUNE_NOME_MANCANTE));
                continue;
            }

            if (!nomiVisti.add(nome.toLowerCase())) {
                result.addErrore(ImportError.of(DomainErrorCode.IMPORT_CAMPO_COMUNE_DUPLICATO, nome));
                continue;
            }

            TipoDato tipoDato = parseTipoDato(dto.tipoDato());
            if (tipoDato == null) {
                result.addErrore(ImportError.of(
                        DomainErrorCode.IMPORT_CAMPO_COMUNE_TIPO_DATO_INVALIDO,
                        nome,
                        dto.tipoDato()));
                continue;
            }

            try {
                catalogoService.addCampoComune(nome, tipoDato, dto.obbligatorio());
                result.incrementCampiComuni();
            } catch (DomainException e) {
                result.addErrore(ImportError.withDomainError(
                        DomainErrorCode.IMPORT_CAMPO_COMUNE_ERRORE_DOMINIO,
                        e,
                        nome));
            }
        }
    }

    private void importaCategorie(List<CategoriaImportDTO> categorie, ImportResult result) {
        Set<String> nomiVisti = new HashSet<>();

        for (CategoriaImportDTO dto : categorie) {
            String nome = dto.nome();

            if (nome == null || nome.isBlank()) {
                result.addErrore(ImportError.of(DomainErrorCode.IMPORT_CATEGORIA_NOME_MANCANTE));
                continue;
            }

            if (!nomiVisti.add(nome.toLowerCase())) {
                result.addErrore(ImportError.of(DomainErrorCode.IMPORT_CATEGORIA_DUPLICATA, nome));
                continue;
            }

            try {
                catalogoService.createCategoria(nome);
            } catch (DomainException e) {
                result.addErrore(ImportError.withDomainError(
                        DomainErrorCode.IMPORT_CATEGORIA_ERRORE_DOMINIO,
                        e,
                        nome));
                continue;
            }

            for (CampoSpecificoImportDTO campoDTO : dto.campiSpecifici()) {
                String nomeCampo = campoDTO.nome();

                if (nomeCampo == null || nomeCampo.isBlank()) {
                    result.addErrore(ImportError.of(
                            DomainErrorCode.IMPORT_CAMPO_SPECIFICO_NOME_MANCANTE,
                            nome));
                    continue;
                }

                TipoDato tipoDato = parseTipoDato(campoDTO.tipoDato());
                if (tipoDato == null) {
                    result.addErrore(ImportError.of(
                            DomainErrorCode.IMPORT_CAMPO_SPECIFICO_TIPO_DATO_INVALIDO,
                            nomeCampo,
                            nome,
                            campoDTO.tipoDato()));
                    continue;
                }

                try {
                    catalogoService.addCampoSpecifico(nome, nomeCampo, tipoDato, campoDTO.obbligatorio());
                } catch (DomainException e) {
                    result.addErrore(ImportError.withDomainError(
                            DomainErrorCode.IMPORT_CAMPO_SPECIFICO_ERRORE_DOMINIO,
                            e,
                            nomeCampo,
                            nome));
                }
            }

            result.incrementCategorie();
        }
    }

    private void importaProposte(List<PropostaImportDTO> proposte, ImportResult result) {
        Set<String> chiaviBatch = new HashSet<>();

        for (PropostaImportDTO dto : proposte) {
            String nomeCategoria = dto.categoria();
            Map<String, String> valori = dto.valoriCampi();
            String titolo = valori.getOrDefault(AppConstants.CAMPO_TITOLO, "");

            if (nomeCategoria == null || nomeCategoria.isBlank()) {
                result.addErrore(ImportError.of(DomainErrorCode.IMPORT_PROPOSTA_CATEGORIA_MANCANTE, titolo));
                continue;
            }

            Categoria categoria = trovaCategoriaPerNome(nomeCategoria);
            if (categoria == null) {
                result.addErrore(ImportError.of(
                        DomainErrorCode.IMPORT_PROPOSTA_CATEGORIA_NON_TROVATA,
                        titolo,
                        nomeCategoria));
                continue;
            }

            String chiave = Proposta.chiaveIdentita(valori);
            if (!chiaviBatch.add(chiave)) {
                result.addErrore(ImportError.of(DomainErrorCode.IMPORT_PROPOSTA_DUPLICATA_FILE, titolo));
                continue;
            }

            List<Campo> campiBase = catalogoService.getCampiBase();
            List<Campo> campiComuni = catalogoService.getCampiComuni();

            List<Campo> tuttiCampi = new ArrayList<>();
            tuttiCampi.addAll(campiBase);
            tuttiCampi.addAll(campiComuni);
            tuttiCampi.addAll(categoria.getCampiSpecifici());

            List<ImportError> erroriTipo = new ArrayList<>();

            for (Campo campo : tuttiCampi) {
                String valore = valori.get(campo.getNome());
                if (valore != null && !valore.isBlank()) {
                    ValidationError errore = DefaultTypeValidator.INSTANCE.validate(valore, campo.getTipoDato());
                    if (errore != null) {
                        erroriTipo.add(ImportError.withValidationError(
                                DomainErrorCode.IMPORT_PROPOSTA_CAMPO_TIPO_NON_VALIDO,
                                errore,
                                titolo,
                                campo.getNome()));
                    }
                }
            }

            if (!erroriTipo.isEmpty()) {
                for (ImportError e : erroriTipo)
                    result.addErrore(e);
                continue;
            }

            try {
                Proposta proposta = propostaService.creaProposta(categoria, campiBase, campiComuni);
                proposta.aggiornaValoriCampi(valori);

                List<ValidationError> erroriValidazione = propostaService.validaProposta(proposta);
                if (!erroriValidazione.isEmpty()) {
                    for (ValidationError e : erroriValidazione)
                        result.addErrore(ImportError.withValidationError(
                                DomainErrorCode.IMPORT_PROPOSTA_VALIDAZIONE,
                                e,
                                titolo));
                    continue;
                }

                propostaService.salvaProposta(proposta);
                result.incrementProposte();

            } catch (DomainException e) {
                result.addErrore(ImportError.withDomainError(
                        DomainErrorCode.IMPORT_PROPOSTA_ERRORE_DOMINIO,
                        e,
                        titolo));
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
