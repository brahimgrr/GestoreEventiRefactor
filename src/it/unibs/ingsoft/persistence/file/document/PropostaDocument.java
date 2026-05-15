package it.unibs.ingsoft.persistence.file.document;

import it.unibs.ingsoft.domain.model.proposta.Proposta;
import it.unibs.ingsoft.domain.model.proposta.PropostaStateChange;
import it.unibs.ingsoft.domain.model.proposta.StatoProposta;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record PropostaDocument(
        String id,
        List<CampoDocument> campiBase,
        List<CampoDocument> campiComuni,
        CategoriaDocument categoria,
        Map<String, String> valoriCampi,
        StatoProposta stato,
        LocalDate dataPubblicazione,
        LocalDate termineIscrizione,
        LocalDate dataEvento,
        List<String> listaAderenti,
        List<PropostaStateChangeDocument> stateHistory) {

    public PropostaDocument {
        campiBase = campiBase == null ? List.of() : List.copyOf(campiBase);
        campiComuni = campiComuni == null ? List.of() : List.copyOf(campiComuni);
        valoriCampi = valoriCampi == null ? Map.of() : Map.copyOf(valoriCampi);
        listaAderenti = listaAderenti == null ? List.of() : List.copyOf(listaAderenti);
        stateHistory = stateHistory == null ? List.of() : List.copyOf(stateHistory);
    }

    public static PropostaDocument fromDomain(Proposta proposta) {
        return new PropostaDocument(
                proposta.getId(),
                proposta.getCampiBase().stream().map(CampoDocument::fromDomain).toList(),
                proposta.getCampiComuni().stream().map(CampoDocument::fromDomain).toList(),
                CategoriaDocument.fromDomain(proposta.getCategoria()),
                proposta.getValoriCampi(),
                proposta.getStato(),
                proposta.getDataPubblicazione(),
                proposta.getTermineIscrizione(),
                proposta.getDataEvento(),
                proposta.getListaAderenti(),
                proposta.getStateHistory().stream()
                        .map(PropostaStateChangeDocument::fromDomain)
                        .toList());
    }

    public Proposta toDomain() {
        List<PropostaStateChange> history = stateHistory.stream()
                .map(PropostaStateChangeDocument::toDomain)
                .toList();
        return Proposta.rehydrate(
                id,
                campiBase.stream().map(CampoDocument::toDomain).toList(),
                campiComuni.stream().map(CampoDocument::toDomain).toList(),
                categoria.toDomain(),
                valoriCampi,
                stato,
                dataPubblicazione,
                termineIscrizione,
                dataEvento,
                listaAderenti,
                history);
    }
}
