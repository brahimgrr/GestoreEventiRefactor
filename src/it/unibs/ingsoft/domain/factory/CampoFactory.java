package it.unibs.ingsoft.domain.factory;

import it.unibs.ingsoft.domain.Campo;
import it.unibs.ingsoft.domain.CampoBaseDefinito;
import it.unibs.ingsoft.domain.TipoCampo;
import it.unibs.ingsoft.domain.TipoDato;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Centralizza la creazione delle definizioni di campo del catalogo.
 */
public final class CampoFactory {

    public List<Campo> creaCampiBase() {
        return Arrays.stream(CampoBaseDefinito.values())
                .map(definizione -> creaCampoBase(definizione.getNomeCampo(), definizione.getTipoDato()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Campo> creaCampiBaseConExtra(List<String> nomiExtra, List<TipoDato> tipiExtra) {
        List<Campo> campi = creaCampiBase();
        campi.addAll(creaCampiBaseExtra(nomiExtra, tipiExtra));
        return campi;
    }

    public List<Campo> creaCampiBaseExtra(List<String> nomiExtra, List<TipoDato> tipiExtra) {
        Objects.requireNonNull(nomiExtra, "I nomi dei campi extra non possono essere null.");
        Objects.requireNonNull(tipiExtra, "I tipi dei campi extra non possono essere null.");

        if (nomiExtra.size() != tipiExtra.size()) {
            throw new IllegalArgumentException("Nomi e tipi dei campi extra devono avere la stessa dimensione.");
        }

        return IntStream.range(0, nomiExtra.size())
                .mapToObj(i -> creaCampoBase(nomiExtra.get(i), tipiExtra.get(i)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public Campo creaCampo(String nome, TipoCampo tipo, TipoDato tipoDato, boolean obbligatorio) {
        return new Campo(nome, tipo, tipoDato, obbligatorio);
    }

    public Campo creaCampoBase(String nome, TipoDato tipoDato) {
        return creaCampo(nome, TipoCampo.BASE, tipoDato, true);
    }

    public Campo creaCampoComune(String nome, TipoDato tipoDato, boolean obbligatorio) {
        return creaCampo(nome, TipoCampo.COMUNE, tipoDato, obbligatorio);
    }

    public Campo creaCampoSpecifico(String nome, TipoDato tipoDato, boolean obbligatorio) {
        return creaCampo(nome, TipoCampo.SPECIFICO, tipoDato, obbligatorio);
    }
}
