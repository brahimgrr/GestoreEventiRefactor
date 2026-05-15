package it.unibs.ingsoft.persistence.file;

import it.unibs.ingsoft.domain.repository.NotificationRepository;
import it.unibs.ingsoft.domain.model.notifica.Notifica;
import it.unibs.ingsoft.persistence.file.document.NotificationStoreDocument;

import java.nio.file.Path;
import java.util.List;

public final class FileNotificationRepository
        extends AbstractFileRepository<NotificationStoreDocument>
        implements NotificationRepository {

    public FileNotificationRepository(Path path) {
        super(path, NotificationStoreDocument.class, NotificationStoreDocument::empty);
    }

    @Override
    public List<Notifica> findByUsername(String username) {
        return super.load().findByUsername(username);
    }

    @Override
    public void add(String username, Notifica notifica) {
        super.save(super.load().add(username, notifica));
    }

    @Override
    public boolean delete(String username, String notificationId) {
        NotificationStoreDocument.DeleteResult result = super.load().delete(username, notificationId);
        if (result.removed()) {
            super.save(result.document());
        }
        return result.removed();
    }
}
