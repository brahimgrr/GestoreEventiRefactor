package it.unibs.ingsoft.application.batch;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unibs.ingsoft.application.batch.dto.*;
import it.unibs.ingsoft.application.catalogo.CatalogoService;
import it.unibs.ingsoft.application.error.ApplicationException;
import it.unibs.ingsoft.application.proposta.PropostaService;
import it.unibs.ingsoft.domain.AppConstants;
import it.unibs.ingsoft.domain.error.DomainException;
import it.unibs.ingsoft.domain.error.ValidationError;
import it.unibs.ingsoft.domain.model.catalogo.Campo;
import it.unibs.ingsoft.domain.model.catalogo.Categoria;
import it.unibs.ingsoft.domain.model.catalogo.TipoDato;
import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.model.proposta.PropostaIdentityPolicy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Gestisce l'importazione batch di campi comuni, categorie e proposte da un file JSON.
 */
public final class BatchImportService {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final CatalogoService catalogoService;
    private final PropostaService propostaService;
    private final PropostaIdentityPolicy identityPolicy;

    public BatchImportService(CatalogoService catalogoService, PropostaService propostaService) {
        this(catalogoService, propostaService, PropostaIdentityPolicy.DEFAULT);
    }

    public BatchImportService(CatalogoService catalogoService,
                              PropostaService propostaService,
                              PropostaIdentityPolicy identityPolicy) {
        this.catalogoService = Objects.requireNonNull(catalogoService);
        this.propostaService = Objects.requireNonNull(propostaService);
        this.identityPolicy = Objects.requireNonNull(identityPolicy);
    }

    private static TipoDato parseTipoDato(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return TipoDato.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public ImportResult importa(Path filePath) {
        if (Files.notExists(filePath))
            throw new ApplicationException(new ImportFailure.FileNotFound(filePath));
        if (!Files.isReadable(filePath))
            throw new ApplicationException(new ImportFailure.FileNotReadable(filePath));

        ImportData data = readImportData(filePath);
        ImportResult result = new ImportResult();

        importaCampiComuni(data.campiComuni(), result);
        importaCategorie(data.categorie(), result);
        importaProposte(data.proposte(), result);

        return result;
    }

    private ImportData readImportData(Path filePath) {
        try {
            return MAPPER.readValue(filePath.toFile(), ImportData.class);
        } catch (IOException e) {
            throw new ApplicationException(new ImportFailure.InvalidJson(filePath), e);
        }
    }

    private void importaCampiComuni(List<CampoImportDTO> campi, ImportResult result) {
        Set<String> nomiVisti = new HashSet<>();

        for (CampoImportDTO dto : campi) {
            String nome = dto.nome();

            if (nome == null || nome.isBlank()) {
                result.addErrore(new ImportError(new ImportFailure.CommonFieldNameMissing()));
                continue;
            }

            if (!nomiVisti.add(nome.toLowerCase())) {
                result.addErrore(new ImportError(new ImportFailure.CommonFieldDuplicated(nome)));
                continue;
            }

            TipoDato tipoDato = parseTipoDato(dto.tipoDato());
            if (tipoDato == null) {
                result.addErrore(new ImportError(new ImportFailure.CommonFieldTypeInvalid(nome, dto.tipoDato())));
                continue;
            }

            try {
                catalogoService.addCampoComune(nome, tipoDato, dto.obbligatorio());
                result.incrementCampiComuni();
            } catch (DomainException e) {
                result.addErrore(new ImportError(new ImportFailure.CommonFieldDomainError(nome, e.failure())));
            }
        }
    }

    private void importaCategorie(List<CategoriaImportDTO> categorie, ImportResult result) {
        Set<String> nomiVisti = new HashSet<>();

        for (CategoriaImportDTO dto : categorie) {
            String nome = dto.nome();

            if (nome == null || nome.isBlank()) {
                result.addErrore(new ImportError(new ImportFailure.CategoryNameMissing()));
                continue;
            }

            if (!nomiVisti.add(nome.toLowerCase())) {
                result.addErrore(new ImportError(new ImportFailure.CategoryDuplicated(nome)));
                continue;
            }

            try {
                catalogoService.createCategoria(nome);
            } catch (DomainException e) {
                result.addErrore(new ImportError(new ImportFailure.CategoryDomainError(nome, e.failure())));
                continue;
            }

            for (CampoSpecificoImportDTO campoDTO : dto.campiSpecifici()) {
                String nomeCampo = campoDTO.nome();

                if (nomeCampo == null || nomeCampo.isBlank()) {
                    result.addErrore(new ImportError(new ImportFailure.SpecificFieldNameMissing(nome)));
                    continue;
                }

                TipoDato tipoDato = parseTipoDato(campoDTO.tipoDato());
                if (tipoDato == null) {
                    result.addErrore(new ImportError(new ImportFailure.SpecificFieldTypeInvalid(
                            nomeCampo,
                            nome,
                            campoDTO.tipoDato())));
                    continue;
                }

                try {
                    catalogoService.addCampoSpecifico(nome, nomeCampo, tipoDato, campoDTO.obbligatorio());
                } catch (DomainException e) {
                    result.addErrore(new ImportError(new ImportFailure.SpecificFieldDomainError(
                            nomeCampo,
                            nome,
                            e.failure())));
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
                result.addErrore(new ImportError(new ImportFailure.ProposalCategoryMissing(titolo)));
                continue;
            }

            Categoria categoria = trovaCategoriaPerNome(nomeCategoria);
            if (categoria == null) {
                result.addErrore(new ImportError(new ImportFailure.ProposalCategoryNotFound(titolo, nomeCategoria)));
                continue;
            }

            String chiave = identityPolicy.chiaveDuplicato(valori);
            if (!chiaviBatch.add(chiave)) {
                result.addErrore(new ImportError(new ImportFailure.ProposalDuplicatedInFile(titolo)));
                continue;
            }

            List<Campo> campiBase = catalogoService.getCampiBase();
            List<Campo> campiComuni = catalogoService.getCampiComuni();

            try {
                Proposta proposta = propostaService.creaProposta(categoria, campiBase, campiComuni);

                List<ValidationError> erroriValidazione =
                        propostaService.applicaValoriEValida(proposta, valori).errori();
                if (!erroriValidazione.isEmpty()) {
                    for (ValidationError e : erroriValidazione)
                        result.addErrore(new ImportError(new ImportFailure.ProposalValidation(titolo, e)));
                    continue;
                }

                propostaService.salvaProposta(proposta);
                result.incrementProposte();

            } catch (DomainException e) {
                result.addErrore(new ImportError(new ImportFailure.ProposalDomainError(titolo, e.failure())));
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
