package fi.vm.sade.organisaatio.service;

import com.github.kagkarlsson.scheduler.ScheduledExecution;
import com.github.kagkarlsson.scheduler.SchedulerClient;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.config.scheduling.KoodistoUpdateTask;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioSearchCriteriaDTOV4;
import fi.vm.sade.organisaatio.service.search.SearchConfig;
import fi.vm.sade.organisaatio.service.search.SearchCriteria;
import fi.vm.sade.organisaatio.service.search.SearchCriteriaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Transactional
public class KoodistoService {

    private final OrganisaatioFindBusinessService organisaatioFindBusinessService;
    private final SearchCriteriaService searchCriteriaService;
    private final SchedulerClient schedulerClient;
    private final KoodistoUpdateTask koodistoUpdateTask;

    public KoodistoService(OrganisaatioFindBusinessService organisaatioFindBusinessService,
                           SearchCriteriaService searchCriteriaService,
                           SchedulerClient schedulerClient,
                           KoodistoUpdateTask koodistoUpdateTask) {
        this.organisaatioFindBusinessService = organisaatioFindBusinessService;
        this.searchCriteriaService = searchCriteriaService;
        this.schedulerClient = schedulerClient;
        this.koodistoUpdateTask = koodistoUpdateTask;
    }

    public void addKoodistoSyncBy(OrganisaatioSearchCriteriaDTOV4 criteriaV4) {
        SearchCriteria criteria = searchCriteriaService.getServiceSearchCriteria(criteriaV4);
        SearchConfig config = new SearchConfig(false, false, false);
        organisaatioFindBusinessService.findBy(criteria, config).stream().map(OrganisaatioPerustieto::getOid)
                .forEach(this::addKoodistoSyncByOid);
    }

    public Collection<String> listKoodistoSyncOids() {
        return schedulerClient.getScheduledExecutionsForTask(koodistoUpdateTask.getName(), String.class).stream().map(ScheduledExecution::getData).collect(Collectors.toList());
    }

    public void addKoodistoSyncByOid(String oid) {
        addKoodistoSyncByOid(oid, Instant.now());
    }

    public void addKoodistoSyncByOid(String oid, Instant instant) {
        schedulerClient.scheduleIfNotExists(koodistoUpdateTask.instance(oid, oid), instant);
    }

    public void removeKoodistoSyncByOid(String oid) {
        schedulerClient.cancel(koodistoUpdateTask.instance(oid, oid));
    }

}
