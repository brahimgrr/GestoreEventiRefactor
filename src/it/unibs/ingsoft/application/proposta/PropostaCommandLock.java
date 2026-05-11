package it.unibs.ingsoft.application.proposta;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public final class PropostaCommandLock {
    private final ReentrantLock lock = new ReentrantLock();

    public void runLocked(Runnable action) {
        Objects.requireNonNull(action);
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }

    public <T> T callLocked(Supplier<T> action) {
        Objects.requireNonNull(action);
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }
}
