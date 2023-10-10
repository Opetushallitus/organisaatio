package fi.vm.sade.organisaatio.config.scheduling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import fi.vm.sade.organisaatio.client.OrganisaatioKoodistoClient;
import fi.vm.sade.organisaatio.model.listeners.ProtectedDataListener;
import fi.vm.sade.organisaatio.service.filters.RequestIdFilter;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class FetchKoodistotTask extends RecurringTask<Void> {
    @Value("${host.virkailija}")
    private String virkailijaHost;
    @Autowired
    private OrganisaatioKoodistoClient koodistoClient;
    @Autowired
    private AuthenticationUtil authenticationUtil;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbc;

    public FetchKoodistotTask() {
        super("FetchKoodistotTask", FixedDelay.of(Duration.ofHours(1)), Void.class, null);
    }

    @Override
    @Transactional
    public void executeRecurringly(TaskInstance<Void> taskInstance, ExecutionContext executionContext) {
        execute();
    }

    protected void execute() {
        try {
            MDC.put("requestId", RequestIdFilter.generateRequestId());
            log.info("Starting FetchKoodistotTask");
            authenticationUtil.configureAuthentication(ProtectedDataListener.ROLE_CRUD_OPH);
            for (String koodisto : List.of("oppilaitostyyppi", "kunta", "posti", "oppilaitoksenopetuskieli")) {
                updateKoodisto(koodisto);
            }
        } finally {
            MDC.remove("requestId");
        }
    }

    private void updateKoodisto(String koodisto) {
        List<KoodistoRow> rows = fetchKoodisto(koodisto)
                .map(this::mapToRow)
                .collect(Collectors.toList());

        String tableName = "koodisto_" + koodisto;
        truncateTable(tableName);
        insertKoodistoRows(tableName, rows);
    }

    private KoodistoRow mapToRow(KoodistoKoodi k) {
        return new KoodistoRow(
                k.getKoodiUri(),
                k.getKoodiArvo(),
                k.getVersio(),
                k.getNimi("fi"),
                k.getNimi("sv")
        );
    }

    private void insertKoodistoRows(String tableName, List<KoodistoRow> rows) {
        String sql = "INSERT INTO " + tableName + "(koodiuri, koodiarvo, versio, nimi_fi, nimi_sv) VALUES (?, ?, ?, ?, ?)";
        jdbc.batchUpdate(sql, rows, 100, (ps, row) -> {
            ps.setString(1, row.getKoodiUri());
            ps.setString(2, row.getKoodiarvo());
            ps.setLong(3, row.getVersio());
            ps.setString(4, row.getNimiFi().orElse(null));
            ps.setString(5, row.getNimiSv().orElse(null));
        });
    }

    private void truncateTable(String tableName) {
        jdbc.execute("TRUNCATE TABLE " + tableName);
    }

    private Stream<KoodistoKoodi> fetchKoodisto(String koodisto) {
        try {
            String koodistoHost = "https://" + virkailijaHost + "/koodisto-service";
            String url = koodistoHost + "/rest/json/" + koodisto + "/koodi?onlyValidKoodis=true";
            log.info("Getting koodisto values from {}", url);
            String json = koodistoClient.get(url);
            return Stream.of(objectMapper.readValue(json, KoodistoKoodi[].class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

@Data
class KoodistoRow {
    private final String koodiUri;
    private final String koodiarvo;
    private final Long versio;
    private final Optional<String> nimiFi;
    private final Optional<String> nimiSv;
}

@Getter
@Setter
class KoodistoKoodi {
    private String koodiUri;
    private String koodiArvo;
    private Long versio;
    private List<KoodistoMetadata> metadata;

    public Optional<String> getNimi(String kieli) {
        return metadata.stream().filter(m -> m.getKieli().equals(kieli.toUpperCase())).findFirst().map(KoodistoMetadata::getNimi);
    }
}

@Getter
@Setter
class KoodistoMetadata {
    private String nimi;
    private String kieli;
}