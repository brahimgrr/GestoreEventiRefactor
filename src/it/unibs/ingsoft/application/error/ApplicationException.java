package it.unibs.ingsoft.application.error;

public class ApplicationException extends IllegalStateException {
    private final ApplicationErrorCode code;

    public ApplicationException(ApplicationErrorCode code) {
        super(code.name());
        this.code = code;
    }

    public ApplicationErrorCode code() {
        return code;
    }
}
