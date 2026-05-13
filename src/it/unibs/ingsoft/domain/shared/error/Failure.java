package it.unibs.ingsoft.domain.shared.error;

/**
 * Semantic failure exposed across architectural boundaries.
 */
public interface Failure {
    default String code() {
        return getClass().getSimpleName();
    }
}
