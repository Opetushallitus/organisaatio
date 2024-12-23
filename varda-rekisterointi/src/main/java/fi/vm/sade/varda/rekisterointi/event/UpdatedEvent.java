package fi.vm.sade.varda.rekisterointi.event;

import fi.vm.sade.varda.rekisterointi.RequestContext;

public class UpdatedEvent<T> extends AuditEvent {

    private final T dtoBefore;
    private final T dtoAfter;

    public UpdatedEvent(RequestContext requestContext, String targetType, Object targetId) {
        this(requestContext, targetType, targetId, null, null);
    }

    public UpdatedEvent(RequestContext requestContext, String targetType, Object targetId, T dtoBefore, T dtoAfter) {
        super(requestContext, targetType, targetId);
        this.dtoBefore = dtoBefore;
        this.dtoAfter = dtoAfter;
    }

    public T getDtoBefore() {
        return dtoBefore;
    }

    public T getDtoAfter() {
        return dtoAfter;
    }
}
