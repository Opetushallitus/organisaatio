package fi.vm.sade.varda.rekisterointi.event;

import fi.vm.sade.varda.rekisterointi.RequestContext;

public class CreatedEvent<T> extends AuditEvent {

    private final T dto;

    public CreatedEvent(RequestContext requestContext, String targetType, Object targetId) {
        this(requestContext, targetType, targetId, null);
    }

    public CreatedEvent(RequestContext requestContext, String targetType, Object targetId, T dto) {
        super(requestContext, targetType, targetId);
        this.dto = dto;
    }

    public T getDto() {
        return dto;
    }
}
