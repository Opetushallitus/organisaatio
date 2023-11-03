package fi.vm.sade.organisaatio.config.scheduling;

import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import fi.vm.sade.organisaatio.client.oiva.Koulutuslupa;
import fi.vm.sade.organisaatio.client.oiva.OivaClient;
import fi.vm.sade.organisaatio.model.listeners.ProtectedDataListener;
import fi.vm.sade.organisaatio.service.filters.RequestIdFilter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
@Slf4j
public class FetchKoulutusluvatTask extends RecurringTask<Void> {
    @Autowired
    private AuthenticationUtil authenticationUtil;
    @Autowired
    private OivaClient oivaClient;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private NamedParameterJdbcTemplate jdbc2;

    public FetchKoulutusluvatTask() {
        super("FetchKoulutusluvatTask", FixedDelay.of(Duration.ofHours(1)), Void.class, null);
    }

    @Override
    @Transactional
    public void executeRecurringly(TaskInstance<Void> taskInstance, ExecutionContext executionContext) {
        try {
            MDC.put("requestId", RequestIdFilter.generateRequestId());
            authenticationUtil.configureAuthentication(ProtectedDataListener.ROLE_CRUD_OPH);
            execute();
        } catch (Exception e) {
            log.info("FetchKoulutusluvatTask failed with exception", e);
            throw e;
        } finally {
            MDC.remove("requestId");
        }
    }

    @Transactional
    public void execute() {
        log.info("Starting {}", getName());
        List<Koulutuslupa> koulutusluvat = oivaClient.getKoulutusluvat();
        log.info("Received {} koulutusluvat from Oiva", koulutusluvat.size());
        Map<String, Long> ytunnusIdMap = getYtunnusMap(koulutusluvat.stream().map(Koulutuslupa::getJarjestajaYtunnus).collect(toList()));
        log.info("Found {} matching organisaatio ytunnus", ytunnusIdMap.size());
        List<JarjestamislupaRow> rows = koulutusluvat.stream()
                .filter(koulutuslupa -> ytunnusIdMap.containsKey(koulutuslupa.getJarjestajaYtunnus()))
                .filter(Koulutuslupa::isVoimassaoleva)
                .flatMap(koulutuslupa -> koulutuslupa.getKoulutukset().stream().map(koulutusKoodiarvo ->
                        new JarjestamislupaRow(
                                ytunnusIdMap.get(koulutuslupa.getJarjestajaYtunnus()),
                                koulutusKoodiarvo,
                                koulutuslupa.getAlkupvm(),
                                koulutuslupa.getLoppupvm()
                        )
                ))
                .collect(toList());

        log.info("Resulting in {} organisaatio-koulutuslupa pairs", rows.size());
        clearExistingKoulutuslupas();
        insertKoulutuslupas(rows);
        log.info("Completed {}", getName());
    }

    private void insertKoulutuslupas(List<JarjestamislupaRow> rows) {
        String sql = "INSERT INTO organisaatio_koulutuslupa(organisaatio_id, koulutuskoodiarvo, alkupvm, loppupvm) VALUES (?, ?, ?, ?)";
        jdbc.batchUpdate(sql, rows, 1000, (ps, row) -> {
            ps.setLong(1, row.getOrganisaatioId());
            ps.setString(2, row.getKoulutusKoodiarvo());
            ps.setDate(3, java.sql.Date.valueOf(row.getAlkupvm()));
            ps.setDate(4, row.getLoppupvm().map(java.sql.Date::valueOf).orElse(null));
        });
    }

    private Map<String, Long> getYtunnusMap(List<String> ytunnukset) {
        if (ytunnukset.isEmpty()) return Map.of();
        return jdbc2.query(
                "SELECT ytunnus, id FROM organisaatio WHERE ytunnus IN (:ytunnukset)",
                Map.of("ytunnukset", ytunnukset),
                (rs, i) -> Map.entry(rs.getString("ytunnus"), rs.getLong("id"))
        ).stream().collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, HashMap::new));
    }

    private void clearExistingKoulutuslupas() {
        jdbc.execute("DELETE FROM organisaatio_koulutuslupa");
    }

    @Data
    private static class JarjestamislupaRow {
        private final Long organisaatioId;
        private final String koulutusKoodiarvo;
        private final LocalDate alkupvm;
        private final Optional<LocalDate> loppupvm;
    }
}
