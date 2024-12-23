package fi.vm.sade.varda.rekisterointi.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fi.vm.sade.auditlog.Audit;
import fi.vm.sade.auditlog.Changes;
import fi.vm.sade.auditlog.Target;
import fi.vm.sade.auditlog.User;
import fi.vm.sade.varda.rekisterointi.Operation;
import fi.vm.sade.varda.rekisterointi.RequestContext;
import fi.vm.sade.varda.rekisterointi.util.RequestContextImpl;
import fi.vm.sade.varda.rekisterointi.event.CreatedEvent;
import fi.vm.sade.varda.rekisterointi.event.DeletedEvent;
import fi.vm.sade.varda.rekisterointi.event.UpdatedEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AuditServiceTest {

    @InjectMocks
    private AuditService auditService;

    @Mock
    private Audit auditMock;

    @Captor
    private ArgumentCaptor<User> userCaptor;
    @Captor
    private ArgumentCaptor<Target> targetCaptor;
    @Captor
    private ArgumentCaptor<Changes> changesCaptor;

    @Test
    public void logCreatedEvent() {
        RequestContext requestContext = new RequestContextImpl("1.2.840.113554.1.2.2", "127.0.0.1");
        TestDto dto = new TestDto("value1");
        CreatedEvent<TestDto> event = new CreatedEvent<>(requestContext, "test", 123, dto);

        auditService.log(event);

        verify(auditMock).log(userCaptor.capture(), eq(Operation.CREATE), targetCaptor.capture(), changesCaptor.capture());
        assertThat(userCaptor.getValue())
                .returns(user("1.2.840.113554.1.2.2", "127.0.0.1"), User::asJson);
        assertThat(targetCaptor.getValue())
                .returns(target(Map.of("type", "test", "id", "123")), Target::asJson);
        assertThat(changesCaptor.getValue())
                .returns(changes(created("property1", "value1")), Changes::asJsonArray);
    }

    @Test
    public void logCreatedEventWithoutDto() {
        RequestContext requestContext = new RequestContextImpl("127.0.0.1");
        CreatedEvent<String> event = new CreatedEvent<>(requestContext, "test", 123);

        auditService.log(event);

        verify(auditMock).log(any(), eq(Operation.CREATE), any(), changesCaptor.capture());
        assertThat(changesCaptor.getValue()).returns(changes(), Changes::asJsonArray);
    }

    @Test
    public void logCreatedEventWithInvalidOid() {
        RequestContext requestContext = new RequestContextImpl("user1", "127.0.0.1");
        CreatedEvent<String> event = new CreatedEvent<>(requestContext, "test", 123);

        auditService.log(event);

        verify(auditMock).log(userCaptor.capture(), eq(Operation.CREATE), any(), any());
        assertThat(userCaptor.getValue()).returns(user(null, "127.0.0.1"), User::asJson);
    }

    @Test
    public void logUpdatedEvent() {
        RequestContext requestContext = new RequestContextImpl("1.2.840.113554.1.2.2", "127.0.0.1");
        TestDto dtoBefore = new TestDto("value1");
        TestDto dtoAfter = new TestDto("value1-updated");
        UpdatedEvent<TestDto> event = new UpdatedEvent<>(requestContext, "test", 123, dtoBefore, dtoAfter);

        auditService.log(event);

        verify(auditMock).log(userCaptor.capture(), eq(Operation.UPDATE), targetCaptor.capture(), changesCaptor.capture());
        assertThat(userCaptor.getValue())
                .returns(user("1.2.840.113554.1.2.2", "127.0.0.1"), User::asJson);
        assertThat(targetCaptor.getValue())
                .returns(target(Map.of("type", "test", "id", "123")), Target::asJson);
        assertThat(changesCaptor.getValue())
                .returns(changes(updated("property1", "value1", "value1-updated")), Changes::asJsonArray);
    }

    @Test
    public void logUpdatedEventWithoutDto() {
        RequestContext requestContext = new RequestContextImpl("127.0.0.1");
        UpdatedEvent<String> event = new UpdatedEvent<>(requestContext, "test", 123);

        auditService.log(event);

        verify(auditMock).log(any(), eq(Operation.UPDATE), any(), changesCaptor.capture());
        assertThat(changesCaptor.getValue()).returns(changes(), Changes::asJsonArray);
    }

    @Test
    public void logUpdatedEventWithInvalidOid() {
        RequestContext requestContext = new RequestContextImpl("user1", "127.0.0.1");
        UpdatedEvent<String> event = new UpdatedEvent<>(requestContext, "test", 123);

        auditService.log(event);

        verify(auditMock).log(userCaptor.capture(), eq(Operation.UPDATE), any(), any());
        assertThat(userCaptor.getValue()).returns(user(null, "127.0.0.1"), User::asJson);
    }

    @Test
    public void logDeletedEvent() {
        RequestContext requestContext = new RequestContextImpl("1.2.840.113554.1.2.2", "127.0.0.1");
        TestDto dto = new TestDto("value1");
        DeletedEvent<TestDto> event = new DeletedEvent<>(requestContext, "test", 123, dto);

        auditService.log(event);

        verify(auditMock).log(userCaptor.capture(), eq(Operation.DELETE), targetCaptor.capture(), changesCaptor.capture());
        assertThat(userCaptor.getValue())
                .returns(user("1.2.840.113554.1.2.2", "127.0.0.1"), User::asJson);
        assertThat(targetCaptor.getValue())
                .returns(target(Map.of("type", "test", "id", "123")), Target::asJson);
        assertThat(changesCaptor.getValue())
                .returns(changes(deleted("property1", "value1")), Changes::asJsonArray);
    }

    @Test
    public void logDeletedEventWithoutDto() {
        RequestContext requestContext = new RequestContextImpl("127.0.0.1");
        DeletedEvent<String> event = new DeletedEvent<>(requestContext, "test", 123);

        auditService.log(event);

        verify(auditMock).log(any(), eq(Operation.DELETE), any(), changesCaptor.capture());
        assertThat(changesCaptor.getValue()).returns(changes(), Changes::asJsonArray);
    }

    @Test
    public void logDeletedEventWithInvalidOid() {
        RequestContext requestContext = new RequestContextImpl("user1", "127.0.0.1");
        DeletedEvent<String> event = new DeletedEvent<>(requestContext, "test", 123);

        auditService.log(event);

        verify(auditMock).log(userCaptor.capture(), eq(Operation.DELETE), any(), any());
        assertThat(userCaptor.getValue()).returns(user(null, "127.0.0.1"), User::asJson);
    }

    private static class TestDto {
        public String property1;
        public TestDto(String property1) {
            this.property1 = property1;
        }
    }

    private static JsonObject user(String oid, String ip) {
        return user(oid, ip, null, null);
    }

    private static JsonObject user(String oid, String ip, String session, String userAgent) {
        JsonObject user = new JsonObject();
        if (oid != null) {
            user.addProperty("oid", oid);
        }
        user.addProperty("ip", ip);
        user.addProperty("session", session);
        user.addProperty("userAgent", userAgent);
        return user;
    }

    private static JsonObject target(Map<String, String> targets) {
        JsonObject target = new JsonObject();
        targets.forEach(target::addProperty);
        return target;
    }

    private static JsonArray changes(JsonObject... change) {
        JsonArray changes = new JsonArray();
        Arrays.stream(change).forEach(changes::add);
        return changes;
    }

    private static JsonObject created(String fieldName, String newValue) {
        JsonObject change = new JsonObject();
        change.addProperty("fieldName", fieldName);
        change.addProperty("newValue", newValue);
        return change;
    }

    private static JsonObject updated(String fieldName, String oldValue, String newValue) {
        JsonObject change = new JsonObject();
        change.addProperty("fieldName", fieldName);
        change.addProperty("oldValue", oldValue);
        change.addProperty("newValue", newValue);
        return change;
    }

    private static JsonObject deleted(String fieldName, String oldValue) {
        JsonObject change = new JsonObject();
        change.addProperty("fieldName", fieldName);
        change.addProperty("oldValue", oldValue);
        return change;
    }

}
