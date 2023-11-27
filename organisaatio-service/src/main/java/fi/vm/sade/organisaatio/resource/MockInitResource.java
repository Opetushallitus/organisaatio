package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.exception.YtunnusException;
import fi.vm.sade.organisaatio.config.scheduling.FetchKoodistotTask;
import fi.vm.sade.organisaatio.config.scheduling.FetchKoulutusluvatTask;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioNimiRDTO;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ConditionalOnProperty(name = "feature.mockapi")
@Hidden
@RestController
@RequestMapping({"/mock/init"})
@RequiredArgsConstructor
@Slf4j
public class MockInitResource {
    private final FetchKoodistotTask fetchKoodistotTask;
    private final FetchKoulutusluvatTask fetchKoulutusluvatTask;
    private final OrganisaatioBusinessService organisaatioBusinessService;

    @PostMapping
    public void createTestData() {
        log.info("Initializing test data");
        fetchKoodistotTask.execute();
        createTestOrganizations();
        fetchKoulutusluvatTask.execute();
    }

    private void createTestOrganizations() {
        log.info("Creating test organizations");

        try {
            var nimi = "Helsingin kaupunki";
            var date = Date.valueOf("1900-01-01");
            var o = new OrganisaatioRDTOV4();
            o.setNimi(Map.of("fi", nimi, "sv", nimi, "en", nimi));
            var orgnimi = new OrganisaatioNimiRDTO();
            orgnimi.setNimi(o.getNimi());
            orgnimi.setAlkuPvm(date);
            o.setAlkuPvm(date);
            o.setKotipaikkaUri("kunta_091");
            o.setNimet(List.of(orgnimi));
            o.setTyypit(Set.of(OrganisaatioTyyppi.KOULUTUSTOIMIJA.koodiValue()));
            o.setYTunnus("0201256-6");
            organisaatioBusinessService.saveOrUpdate(o);
        } catch (YtunnusException e) {
            log.info("Test organization already exists", e);
        }
    }
}

