package fi.vm.sade.organisaatio.email;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
public class QueuedEmail {
    private String id;
    private String status;
    private List<String> recipients;
    private String replyTo;
    private String subject;
    private String body;
    private String lahetysTunniste;
    private Timestamp created;
    private Timestamp modified;
}
