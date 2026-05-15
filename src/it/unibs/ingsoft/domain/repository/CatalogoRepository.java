package it.unibs.ingsoft.domain.repository;

import it.unibs.ingsoft.domain.model.catalogo.Catalogo;

import java.util.function.Function;

public interface CatalogoRepository {
    Catalogo load();

    void save(Catalogo catalogo);

    default <R> R update(Function<Catalogo, R> operation) {
        Catalogo catalogo = load();
        R result = operation.apply(catalogo);
        save(catalogo);
        return result;
    }
}
