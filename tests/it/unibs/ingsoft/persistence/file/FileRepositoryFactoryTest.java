package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.persistence.interfaces.IBachecaRepository;
import it.unibs.ingsoft.persistence.interfaces.ICatalogoRepository;
import it.unibs.ingsoft.persistence.interfaces.ICredenzialiRepository;
import it.unibs.ingsoft.persistence.interfaces.ISpazioPersonaleRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileRepositoryFactoryTest {
    @Test
    void getInstance_quandoInvocatoDueVolte_restituisceStessaIstanza() {
        assertSame(FileRepositoryFactory.getInstance(), FileRepositoryFactory.getInstance());
    }

    @Test
    void createCatalogoRepository_quandoInvocato_restituisceRepositoryCatalogoSuFile() {
        ICatalogoRepository repository = FileRepositoryFactory.getInstance().createCatalogoRepository();

        assertInstanceOf(FileCatalogoRepository.class, repository);
    }

    @Test
    void createCredenzialiRepository_quandoInvocato_restituisceRepositoryCredenzialiSuFile() {
        ICredenzialiRepository repository = FileRepositoryFactory.getInstance().createCredenzialiRepository();

        assertInstanceOf(FileCredenzialiRepository.class, repository);
    }

    @Test
    void createBachecaRepository_quandoInvocato_restituisceRepositoryBachecaSuFile() {
        IBachecaRepository repository = FileRepositoryFactory.getInstance().createBachecaRepository();

        assertInstanceOf(FileBachecaRepository.class, repository);
    }

    @Test
    void createSpazioPersonaleRepository_quandoInvocato_restituisceRepositorySpazioPersonaleSuFile() {
        ISpazioPersonaleRepository repository = FileRepositoryFactory.getInstance().createSpazioPersonaleRepository();

        assertInstanceOf(FileSpazioPersonaleRepository.class, repository);
    }
}
