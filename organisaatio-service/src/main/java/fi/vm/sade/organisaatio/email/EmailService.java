package fi.vm.sade.organisaatio.email;

import fi.vm.sade.organisaatio.client.viestinvalitys.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private static final int SAILYTYSAIKA = 365;
    private static final Lahettaja OSOITEPALVELU_LAHETTAJA = Lahettaja.builder()
            .nimi("Opetushallitus")
            .sahkopostiOsoite("noreply@opintopolku.fi")
            .build();

    private final ViestinvalitysClient viestinvalitysClient;
    private final JdbcTemplate jdbcTemplate;

    public String queueEmail(QueuedEmail email) {
        var emailId = UUID.randomUUID().toString();
        log.info("Queueing email with subject and id: {}, {}", email.getSubject(), emailId);
        var sql = """
                INSERT INTO queuedemail (id, queuedemailstatus_id, recipients, replyto, subject, body)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(sql);
            ps.setString(1, emailId);
            ps.setString(2, "QUEUED");
            ps.setArray(3, con.createArrayOf("text", email.getRecipients().toArray()));
            ps.setString(4, email.getReplyTo());
            ps.setString(5, email.getSubject());
            ps.setString(6, email.getBody());
            return ps;
        });

        return emailId;
    }

    private final TransactionTemplate transactionTemplate;

    public void attemptSendingEmail(String emailId) {
        log.info("Attempting to send email {}", emailId);
        transactionTemplate.execute(status -> {
            getQueuedEmailForSending(emailId).ifPresent(email -> {
                // TODO: Yli 2048 vastaanottajan mailit
                var response = viestinvalitysClient.luoViesti(Viesti.builder()
                        .lahettaja(OSOITEPALVELU_LAHETTAJA)
                        .replyTo(email.getReplyTo())
                        .vastaanottajat(email.getRecipients().stream().map(s -> Vastaanottaja.builder().sahkopostiOsoite(s).build()).toList())
                        .otsikko(email.getSubject())
                        .sisalto(email.getBody())
                        .sisallonTyyppi(SisallonTyyppi.html)
                        .lahettavaPalvelu("osoitepalvelu")
                        .prioriteetti(Prioriteetti.normaali)
                        .sailytysaika(SAILYTYSAIKA)
                        .build());
                log.info("Sent email with lähetystunniste: {}", response.getLahetysTunniste());
                markEmailAsSent(emailId, response.getLahetysTunniste());
            });
            return null;
        });
    }

    private Optional<QueuedEmail> getQueuedEmailForSending(String emailId) {
        var sql = "SELECT * FROM queuedemail WHERE id = ? AND queuedemailstatus_id = 'QUEUED' FOR UPDATE";
        var results = jdbcTemplate.query(sql, (rs, rowNum) -> QueuedEmail.builder()
                        .id(rs.getString("id"))
                        .status(rs.getString("queuedemailstatus_id"))
                        .recipients(Arrays.asList((String[]) rs.getArray("recipients").getArray()))
                        .replyTo(rs.getString("replyto"))
                        .subject(rs.getString("subject"))
                        .body(rs.getString("body"))
                        .created(rs.getTimestamp("created"))
                        .modified(rs.getTimestamp("modified"))
                        .build(),
                emailId);
        return results.stream().findFirst();
    }

    private void markEmailAsSent(String emailId, String lahetystunniste) {
        var sql = "UPDATE queuedemail SET lahetystunniste = ?, modified = current_timestamp WHERE id = ?";
        jdbcTemplate.update(sql, lahetystunniste, emailId);
    }
}
