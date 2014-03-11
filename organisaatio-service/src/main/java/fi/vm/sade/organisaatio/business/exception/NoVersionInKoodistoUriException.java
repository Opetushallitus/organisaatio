package fi.vm.sade.organisaatio.business.exception;

import fi.vm.sade.generic.service.exception.SadeBusinessException;

/**
 * Exception luokka tilanteeseen, jossa organisaatioon tallennettavassa datassa on koodistouri ilman versiota.
 * 
 * @author simok
 * 
 */
public class NoVersionInKoodistoUriException extends SadeBusinessException {

    private static final long serialVersionUID = 1L;
    public static final String ERROR_KEY = NoVersionInKoodistoUriException.class.getCanonicalName();

    public NoVersionInKoodistoUriException() {
        super();
    }

    public NoVersionInKoodistoUriException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoVersionInKoodistoUriException(String message) {
        super(message);
    }

    public NoVersionInKoodistoUriException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getErrorKey() {
        return ERROR_KEY;
    }

}
