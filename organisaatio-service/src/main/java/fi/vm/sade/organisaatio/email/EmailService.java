package fi.vm.sade.organisaatio.email;

import fi.vm.sade.organisaatio.client.viestinvalitys.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private static final int MAX_BATCH_SIZE = 512;
    private static final int SAILYTYSAIKA = 365;
    private static final Lahettaja OSOITEPALVELU_LAHETTAJA = Lahettaja.builder()
            .nimi("Opetushallitus")
            .sahkopostiOsoite("noreply@opintopolku.fi")
            .build();

    private final ViestinvalitysClient viestinvalitysClient;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    private final List<KayttooikeusRajoitukset> kayttooikeusRajoitukset = List.of(
        new KayttooikeusRajoitukset("1.2.246.562.10.00000000001", "APP_OSOITE_CRUD")
    );

    public String saveAttachment(String hakutulosId, MultipartFile file) {
        String liiteTunniste = viestinvalitysClient.postAttachment(file).getLiiteTunniste();
        log.info("Email attachment {} saved with hakutulos {}", liiteTunniste, hakutulosId);
        return liiteTunniste;
    }

    public String queueEmail(QueuedEmail email) {
        var emailId = UUID.randomUUID().toString();
        log.info("Queueing email with subject and id: {}, {}", email.getSubject(), emailId);
        var sql = """
                INSERT INTO queuedemail (id, osoitteet_haku_and_hakutulos_id, queuedemailstatus_id, recipients, copy, replyto, subject, body, attachment_ids, idempotency_key)
                VALUES (?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?, ?, ?::uuid)
                """;

        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(sql);
            var col = 1;
            ps.setString(col++, emailId);
            ps.setString(col++, email.getHakutulosId());
            ps.setString(col++, "QUEUED");
            ps.setArray(col++, con.createArrayOf("text", email.getRecipients().toArray()));
            ps.setString(col++, email.getCopy());
            ps.setString(col++, email.getReplyTo());
            ps.setString(col++, email.getSubject());
            ps.setString(col++, email.getBody());
            if (email.getAttachmentIds() != null && !email.getAttachmentIds().isEmpty()) {
                ps.setArray(col++, con.createArrayOf("text", email.getAttachmentIds().toArray()));
            } else {
                ps.setArray(col++, null);
            }
            ps.setString(col++, UUID.randomUUID().toString());
            return ps;
        });

        return emailId;
    }

    public void attemptSendingEmail(String emailId) {
        try {
            String lahetystunniste = transactionTemplate.execute(status -> sendLahetys(emailId));
            boolean allSent = false;
            do {
                allSent = transactionTemplate.execute(status -> sendViestiInBatches(emailId, lahetystunniste));
            } while (!allSent);
        } catch (BadRequestException bre) {
            log.info("Malformed email request sent " + emailId, bre);
            markEmailInvalid(emailId);
            throw bre;
        } catch (Exception e) {
            log.warn("Failed to send email " + emailId, e);
            updateLastAttempt(emailId);
        }
    }

    private String sendLahetys(String emailId) {
        log.info("Attempting to send lähetys for email {}", emailId);
        var email = queryEmails("WHERE queuedemail.id = ?::uuid AND queuedemailstatus_id = 'QUEUED' FOR UPDATE OF queuedemail", emailId).stream().findFirst().orElseThrow();
        if (email.getLahetysTunniste() != null) {
            return email.getLahetysTunniste();
        }
        var lahetys = Lahetys.builder()
                .lahettaja(OSOITEPALVELU_LAHETTAJA)
                .replyTo(email.getReplyTo())
                .lahettavanVirkailijanOid(email.getVirkailijaOid())
                .otsikko(email.getSubject())
                .lahettavaPalvelu("organisaatiopalvelu")
                .prioriteetti(Prioriteetti.normaali)
                .sailytysaika(SAILYTYSAIKA)
                .build();
        var response = viestinvalitysClient.luoLahetys(lahetys);
        log.info("Sent lähetys {} and received lähetystunniste {}", emailId, response.getLahetysTunniste());
        updateLahetysTunniste(emailId, response.getLahetysTunniste());
        return response.getLahetysTunniste();
    }

    private boolean sendViestiInBatches(String emailId, String lahetystunniste) {
        var email = queryEmails("WHERE queuedemail.id = ?::uuid AND queuedemailstatus_id = 'QUEUED' FOR UPDATE OF queuedemail", emailId).stream().findFirst().orElseThrow();
        var recipients = new ArrayList<>(email.getRecipients());
        if (email.getCopy() != null) recipients.add(email.getCopy());
        var lastRecipientIndex = recipients.size() <= email.getBatchSent() + MAX_BATCH_SIZE
                ? recipients.size()
                : email.getBatchSent() + MAX_BATCH_SIZE;
        var batchRecipients = recipients.subList(email.getBatchSent(), lastRecipientIndex);
        log.info("Attempting to send viesti for email {} starting from recipient {} with batch size {}", emailId, email.getBatchSent(), batchRecipients.size());
        viestinvalitysClient.luoViesti(getViesti(email, batchRecipients, lahetystunniste));
        log.info("Sent viesti {} and received lähetystunniste {}", emailId, lahetystunniste);
        if (recipients.size() <= email.getBatchSent() + MAX_BATCH_SIZE) {
            markEmailAsSent(emailId, email.getBatchSent() + batchRecipients.size());
            return true;
        } else {
            markEmailBatchSent(emailId, email.getBatchSent() + batchRecipients.size());
            return false;
        }
    }

    public Viesti getViesti(QueuedEmail email, List<String> recipients, String lahetystunniste) {
        return Viesti.builder()
            .lahetysTunniste(lahetystunniste)
            .vastaanottajat(recipients.stream().map(s -> Vastaanottaja.builder().sahkopostiOsoite(s).build()).toList())
            .otsikko(email.getSubject())
            .sisalto(email.getBody())
            .sisallonTyyppi(SisallonTyyppi.html)
            .liitteidenTunnisteet(email.getAttachmentIds())
            .kayttooikeusRajoitukset(kayttooikeusRajoitukset)
            .idempotencyKey(email.getIdempotencyKey())
            .build();
    }

    public Optional<QueuedEmail> getEmail(String emailId) {
        return queryEmails("WHERE queuedemail.id = ?::uuid", emailId).stream().findFirst();
    }

    public List<String> getQueuedEmailIdsToRetry() {
        var sql = """
                SELECT id
                FROM queuedemail
                WHERE queuedemailstatus_id = 'QUEUED'
                  AND (last_attempt IS NULL OR last_attempt < current_timestamp - INTERVAL '10 minutes')
                LIMIT 10
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("id"));
    }

    private List<QueuedEmail> queryEmails(String where, String emailId) {
        var select = """
                SELECT queuedemail.id, lahetystunniste, queuedemailstatus_id, copy, recipients, replyto, subject, body, last_attempt, sent_at, created, modified, virkailija_oid, attachment_ids, batch_sent, idempotency_key
                FROM queuedemail
                LEFT JOIN osoitteet_haku_and_hakutulos ON osoitteet_haku_and_hakutulos.id = queuedemail.osoitteet_haku_and_hakutulos_id
                """;
        var sql = String.join("\n", List.of(select, where));
        return jdbcTemplate.query(sql, queuedEmailRowMapper, emailId);
    }

    private final RowMapper<QueuedEmail> queuedEmailRowMapper = (rs, rowNum) -> QueuedEmail.builder()
            .id(rs.getString("id"))
            .lahetysTunniste(rs.getString("lahetystunniste"))
            .copy(rs.getString("copy"))
            .status(rs.getString("queuedemailstatus_id"))
            .recipients(Arrays.asList((String[]) rs.getArray("recipients").getArray()))
            .replyTo(rs.getString("replyto"))
            .subject(rs.getString("subject"))
            .body(rs.getString("body"))
            .virkailijaOid(rs.getString("virkailija_oid"))
            .lastAttempt(rs.getTimestamp("last_attempt"))
            .sentAt(rs.getTimestamp("sent_at"))
            .created(rs.getTimestamp("created"))
            .modified(rs.getTimestamp("modified"))
            .attachmentIds(rs.getArray("attachment_ids") != null
                ? Arrays.asList((String[]) rs.getArray("attachment_ids").getArray())
                : null)
            .batchSent(rs.getInt("batch_sent"))
            .idempotencyKey(rs.getString("idempotency_key"))
            .build();

    private void markEmailInvalid(String emailId) {
        var sql = """
                UPDATE queuedemail SET
                    queuedemailstatus_id = 'ERROR',
                    last_attempt = current_timestamp,
                    modified = current_timestamp
                WHERE id = ?::uuid
                """;
        jdbcTemplate.update(sql, emailId);
    }

    private void updateLastAttempt(String emailId) {
        var sql = """
                UPDATE queuedemail SET
                    last_attempt = current_timestamp,
                    modified = current_timestamp
                WHERE id = ?::uuid
                """;
        jdbcTemplate.update(sql, emailId);
    }

    private void updateLahetysTunniste(String emailId, String lahetystunniste) {
        var sql = """
                UPDATE queuedemail SET
                    lahetystunniste = ?,
                    modified = current_timestamp
                WHERE id = ?::uuid
                """;
        jdbcTemplate.update(sql, lahetystunniste, emailId);
    }

    private void markEmailBatchSent(String emailId, int batchSize) {
        var sql = """
                UPDATE queuedemail SET
                    last_attempt = current_timestamp,
                    modified = current_timestamp,
                    idempotency_key = ?::uuid,
                    batch_sent = ?
                WHERE id = ?::uuid
                """;
        jdbcTemplate.update(sql, UUID.randomUUID().toString(), batchSize, emailId);
    }

    private void markEmailAsSent(String emailId, int batchSize) {
        var sql = """
                UPDATE queuedemail SET
                    queuedemailstatus_id = 'SENT',
                    last_attempt = current_timestamp,
                    sent_at = current_timestamp,
                    batch_sent = ?,
                    modified = current_timestamp
                WHERE id = ?::uuid
                """;
        jdbcTemplate.update(sql, batchSize, emailId);
    }
}
