package fi.vm.sade.varda.rekisterointi.event;

import fi.vm.sade.varda.rekisterointi.RequestContext;

public class DeletedEvent<T> extends AuditEvent {

    private final T dto;

    public DeletedEvent(RequestContext requestContext, String targetType, Object targetId) {
        this(requestContext, targetType, targetId, null);
    }

    public DeletedEvent(RequestContext requestContext, String targetType, Object targetId, T dto) {
        super(requestContext, targetType, targetId);
        this.dto = dto;
    }

    public T getDto() {
        return dto;
    }
}
