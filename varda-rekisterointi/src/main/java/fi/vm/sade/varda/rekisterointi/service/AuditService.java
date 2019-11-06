package fi.vm.sade.varda.rekisterointi.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.vm.sade.auditlog.Audit;
import fi.vm.sade.auditlog.Changes;
import fi.vm.sade.auditlog.Target;
import fi.vm.sade.auditlog.User;
import fi.vm.sade.varda.rekisterointi.Operation;
import fi.vm.sade.varda.rekisterointi.RequestContext;
import fi.vm.sade.varda.rekisterointi.event.AuditEvent;
import fi.vm.sade.varda.rekisterointi.event.CreatedEvent;
import fi.vm.sade.varda.rekisterointi.event.DeletedEvent;
import fi.vm.sade.varda.rekisterointi.event.UpdatedEvent;
import fi.vm.sade.varda.rekisterointi.util.AuditUtils;
import lombok.RequiredArgsConstructor;
import org.ietf.jgss.Oid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.net.InetAddress;
import java.util.Optional;
import java.util.function.BiConsumer;

import static fi.vm.sade.varda.rekisterointi.util.AuditUtils.createInetAddress;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final Audit audit;
    private final Gson gson = new Gson();

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void log(CreatedEvent<?> event) {
        log(event, Operation.CREATE, changes(event.getDto(), Changes.Builder::added));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void log(UpdatedEvent<?> event) {
        log(event, Operation.UPDATE, Changes.updatedDto(event.getDtoAfter(), event.getDtoBefore()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void log(DeletedEvent<?> event) {
        log(event, Operation.DELETE, changes(event.getDto(), Changes.Builder::removed));
    }

    private void log(AuditEvent event, Operation operation, Changes changes) {
        RequestContext requestContext = event.getRequestContext();
        User user = createUser(requestContext);
        Target.Builder targetBuilder = new Target.Builder()
                .setField("type", event.getTargetType())
                .setField("id", String.valueOf(event.getTargetId()));
        audit.log(user, operation, targetBuilder.build(), changes);
    }

    private User createUser(RequestContext requestContext) {
        Optional<Oid> oid = requestContext.getUsername().flatMap(AuditUtils::createOid);
        InetAddress ip = createInetAddress(requestContext.getIp());
        Optional<String> session = requestContext.getSession();
        Optional<String> userAgent = requestContext.getUserAgent();
        return new User(oid.orElse(null), ip, session.orElse(null), userAgent.orElse(null));
    }

    private Changes changes(Object dto, BiConsumer<Changes.Builder, JsonObject> consumer) {
        Changes.Builder changesBuilder = new Changes.Builder();
        JsonElement jsonElement = gson.toJsonTree(dto);
        if (jsonElement.isJsonObject()) {
            consumer.accept(changesBuilder, jsonElement.getAsJsonObject());
        }
        return changesBuilder.build();
    }

}
