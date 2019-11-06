package fi.vm.sade.varda.rekisterointi.event;

import fi.vm.sade.varda.rekisterointi.RequestContext;

public abstract class AuditEvent {

    private final RequestContext requestContext;
    private final String targetType;
    private final Object targetId;

    public AuditEvent(RequestContext requestContext, String targetType, Object targetId) {
        this.requestContext = requestContext;
        this.targetType = targetType;
        this.targetId = targetId;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

    public String getTargetType() {
        return targetType;
    }

    public Object getTargetId() {
        return targetId;
    }
}
