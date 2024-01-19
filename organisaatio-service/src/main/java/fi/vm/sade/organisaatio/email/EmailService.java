package fi.vm.sade.organisaatio.email;

import fi.vm.sade.organisaatio.client.viestinvalitys.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

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
    private final TransactionTemplate transactionTemplate;

    public String queueEmail(QueuedEmail email) {
        var emailId = UUID.randomUUID().toString();
        log.info("Queueing email with subject and id: {}, {}", email.getSubject(), emailId);
        var sql = """
                INSERT INTO queuedemail (id, osoitteet_haku_and_hakutulos_id, queuedemailstatus_id, recipients, copy, replyto, subject, body)
                VALUES (?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?)
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
            return ps;
        });

        return emailId;
    }

    public void attemptSendingEmail(String emailId) {
        log.info("Attempting to send email {}", emailId);
        transactionTemplate.execute(status -> {
            queryEmail("WHERE queuedemail.id = ?::uuid AND queuedemailstatus_id = 'QUEUED' FOR UPDATE", emailId).ifPresent(email -> {
                var recipients = new ArrayList<>(email.getRecipients());
                if (email.getCopy() != null) recipients.add(email.getCopy());
                // TODO: Yli 2048 vastaanottajan mailit
                var response = viestinvalitysClient.luoViesti(Viesti.builder()
                        .lahettaja(OSOITEPALVELU_LAHETTAJA)
                        .replyTo(email.getReplyTo())
                        .lahettavanVirkailijanOid(email.getVirkailijaOid())
                        .vastaanottajat(recipients.stream().map(s -> Vastaanottaja.builder().sahkopostiOsoite(s).build()).toList())
                        .otsikko(email.getSubject())
                        .sisalto(email.getBody())
                        .sisallonTyyppi(SisallonTyyppi.text)
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

    public Optional<QueuedEmail> getEmail(String emailId) {
        return queryEmail("WHERE queuedemail.id = ?::uuid", emailId);
    }

    private Optional<QueuedEmail> queryEmail(String where, String emailId) {
        var select = """
                SELECT queuedemail.id, lahetystunniste, queuedemailstatus_id, copy, recipients, replyto, subject, body, created, modified, virkailija_oid
                FROM queuedemail JOIN osoitteet_haku_and_hakutulos ON osoitteet_haku_and_hakutulos.id = queuedemail.osoitteet_haku_and_hakutulos_id
                """;
        var sql = String.join("\n", List.of(select, where));
        var results = jdbcTemplate.query(sql, (rs, rowNum) -> QueuedEmail.builder()
                        .id(rs.getString("id"))
                        .lahetysTunniste(rs.getString("lahetystunniste"))
                        .copy(rs.getString("copy"))
                        .status(rs.getString("queuedemailstatus_id"))
                        .recipients(Arrays.asList((String[]) rs.getArray("recipients").getArray()))
                        .replyTo(rs.getString("replyto"))
                        .subject(rs.getString("subject"))
                        .body(rs.getString("body"))
                        .virkailijaOid(rs.getString("virkailija_oid"))
                        .created(rs.getTimestamp("created"))
                        .modified(rs.getTimestamp("modified"))
                        .build(),
                emailId);
        return results.stream().findFirst();
    }

    private void markEmailAsSent(String emailId, String lahetystunniste) {
        var sql = "UPDATE queuedemail SET queuedemailstatus_id = 'SENT', lahetystunniste = ?, modified = current_timestamp WHERE id = ?::uuid";
        jdbcTemplate.update(sql, lahetystunniste, emailId);
    }
}
