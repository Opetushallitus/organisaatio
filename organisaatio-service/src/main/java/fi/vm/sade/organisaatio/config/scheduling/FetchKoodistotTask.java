package fi.vm.sade.organisaatio.config.scheduling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.FailureHandler;
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
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

@Component
@Slf4j
public class FetchKoodistotTask extends RecurringTask<Void> {
    @Value("${koodisto.baseurl}")
    private String koodistoBaseurl;
    @Autowired
    private OrganisaatioKoodistoClient koodistoClient;
    @Autowired
    private AuthenticationUtil authenticationUtil;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbc;

    public FetchKoodistotTask() {
        super("FetchKoodistotTask", FixedDelay.of(Duration.ofHours(1)), Void.class, (FailureHandler<Void>) null);
    }

    @Override
    @Transactional
    public void executeRecurringly(TaskInstance<Void> taskInstance, ExecutionContext executionContext) {
        try {
            MDC.put("requestId", RequestIdFilter.generateRequestId());
            authenticationUtil.configureAuthentication(ProtectedDataListener.ROLE_CRUD_OPH);
            execute();
        } catch (Exception e) {
            log.info("FetchKoodistotTask failed with exception", e);
            throw e;
        } finally {
            MDC.remove("requestId");
        }
    }

    public void execute() {
        log.info("Starting FetchKoodistotTask");
        for (String koodisto : List.of("oppilaitostyyppi", "posti", "oppilaitoksenopetuskieli", "vuosiluokat")) {
            updateKoodisto(koodisto);
        }
        updateKoulutusKoodisto();
        updateMaakuntaKuntaRelaatiot();
    }

    @SuppressWarnings("java:S2077") // koodisto is not user input and is safe to use in SQL
    private void updateKoodisto(String koodisto) {
        List<KoodistoRow> rows = fetchKoodisto(koodisto, true)
                .map(this::mapToRow)
                .collect(toList());

        String tableName = "koodisto_" + koodisto;
        jdbc.execute("DELETE FROM " + tableName);
        insertKoodistoRows(tableName, rows);
    }

    private void updateKoulutusKoodisto() {
        List<KoodistoRow> rows = fetchKoodisto("koulutus", false)
                .map(this::mapToRow)
                .collect(toList());

        jdbc.execute("DELETE FROM koodisto_koulutus WHERE NOT exists (SELECT * FROM organisaatio_koulutuslupa WHERE koulutuskoodiarvo = koodiarvo)");
        insertKoodistoRows("koodisto_koulutus", rows);
    }

    private void updateMaakuntaKuntaRelaatiot() {
        List<Relaatio> maakuntaKoodis = fetchKoodistoWithRelations("maakunta", 2L).collect(toList());
        List<KoodistoRow> maakuntaRows = maakuntaKoodis.stream().map(this::mapToRow).collect(toList());
        List<KoodistoRow> kuntaRows = fetchKoodisto("kunta", true).map(this::mapToRow).collect(toList());
        TreeSet<String> kuntaUris = kuntaRows.stream().map(KoodistoRow::getKoodiUri).collect(toCollection(TreeSet::new));

        List<Pair<String, String>> relaatiot = maakuntaKoodis.stream().flatMap(k -> {
            String maakuntaUri = k.getKoodiUri();
            return k.getWithinCodeElements().stream()
                    // Some of the koodis given as relations are not valid anymore, so we need to filter them out
                    // e.g. kunta_223 Karjalohja is not valid since 2012 or so
                    .filter(r -> kuntaUris.contains(r.getCodeElementUri()))
                    .map(r -> {
                        String kuntaUri = r.getCodeElementUri();
                        return Pair.of(maakuntaUri, kuntaUri);
                    });
        }).collect(toList());

        jdbc.execute("TRUNCATE TABLE koodisto_maakunta, koodisto_kunta, maakuntakuntarelation");
        insertKoodistoRows("koodisto_kunta", kuntaRows);
        insertKoodistoRows("koodisto_maakunta", maakuntaRows);
        String sql = "INSERT INTO maakuntakuntarelation(maakuntauri, kuntauri) VALUES (?, ?)";
        jdbc.batchUpdate(sql, relaatiot, 100, (ps, row) -> {
            ps.setString(1, row.getLeft());
            ps.setString(2, row.getRight());
        });
    }


    private Stream<Relaatio> fetchKoodistoWithRelations(String koodisto, Long version) {
        try {
            String url = koodistoBaseurl + "/rest/codeelement/codes/withrelations/" + koodisto + "/" + version;
            log.info("Getting koodisto values from {}", url);
            String json = koodistoClient.get(url);
            return Stream.of(objectMapper.readValue(json, Relaatio[].class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private KoodistoRow mapToRow(Relaatio k) {
        return new KoodistoRow(
                k.getKoodiUri(),
                k.getKoodiArvo(),
                k.getVersio(),
                k.getNimi("fi"),
                k.getNimi("sv")
        );
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

    @SuppressWarnings("java:S2077") // tableName is not user input and is safe to use in SQL
    private void insertKoodistoRows(String tableName, List<KoodistoRow> rows) {
        String sql = "INSERT INTO " + tableName + "(koodiuri, koodiarvo, versio, nimi_fi, nimi_sv) VALUES (?, ?, ?, ?, ?) ON CONFLICT DO NOTHING";
        jdbc.batchUpdate(sql, rows, 100, (ps, row) -> {
            ps.setString(1, row.getKoodiUri());
            ps.setString(2, row.getKoodiarvo());
            ps.setLong(3, row.getVersio());
            ps.setString(4, row.getNimiFi().orElse(null));
            ps.setString(5, row.getNimiSv().orElse(null));
        });
    }

    private Stream<KoodistoKoodi> fetchKoodisto(String koodisto, boolean onlyValidKoodis) {
        try {
            String url = koodistoBaseurl + "/rest/json/" + koodisto + "/koodi?onlyValidKoodis=" + (onlyValidKoodis ? "true" : "false");
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

@Getter
@Setter
class Relaatio {
    List<KoodistoMetadata> metadata;
    List<RelaatioTieto> withinCodeElements;
    String koodiUri;
    Long versio;
    String koodiArvo;

    public Optional<String> getNimi(String kieli) {
        return metadata.stream().filter(m -> m.getKieli().equals(kieli.toUpperCase())).findFirst().map(KoodistoMetadata::getNimi);
    }
}

@Getter
@Setter
class RelaatioTieto {
    String codeElementUri;
    String codeElementVersion;
    boolean passive;
}
