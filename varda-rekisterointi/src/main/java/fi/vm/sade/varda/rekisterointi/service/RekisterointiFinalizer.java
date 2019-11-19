package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.client.OrganisaatioClient;
import fi.vm.sade.varda.rekisterointi.exception.InvalidInputException;
import fi.vm.sade.varda.rekisterointi.model.Organisaatio;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioV4Dto;
import fi.vm.sade.varda.rekisterointi.model.Paatos;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
@Transactional
public class RekisterointiFinalizer {

    static final String VARDA_TOIMINTAMUOTO = "vardatoimintamuoto_tm01";
    private static final Logger LOGGER = LoggerFactory.getLogger(RekisterointiFinalizer.class);

    private final OrganisaatioService organisaatioService;
    private final OrganisaatioClient organisaatioClient;

    public RekisterointiFinalizer(OrganisaatioService organisaatioService,
                                  OrganisaatioClient organisaatioClient) {
        this.organisaatioService = organisaatioService;
        this.organisaatioClient = organisaatioClient;
    }

    public void finalize(Rekisterointi rekisterointi) {
        Organisaatio organisaatio = rekisterointi.organisaatio;
        if (organisaatio.oid != null) {
            lisaaVardaToimintamuoto(organisaatio);
        } else {
            luoOrganisaatio(organisaatio);
        }
    }

    void luoOrganisaatio(Organisaatio organisaatio) {
        OrganisaatioV4Dto dto = organisaatioService.muunnaOrganisaatio(organisaatio);
        OrganisaatioV4Dto luotu = organisaatioClient.create(dto);
        LOGGER.info("Luotu rekisteröinnin pohjalta organisaatio, y-tunnus: {}", luotu.ytunnus);
    }

    void lisaaVardaToimintamuoto(Organisaatio organisaatio) {
        OrganisaatioV4Dto dto = organisaatioClient.getV4ByOid(organisaatio.oid).orElseThrow(
                () -> new InvalidInputException("Organisaatiota ei löydy, oid: " + organisaatio.oid)
        );
        if (!dto.tyypit.contains(VARDA_TOIMINTAMUOTO)) {
            dto.tyypit = new HashSet<>(dto.tyypit);
            dto.tyypit.add(VARDA_TOIMINTAMUOTO);
            organisaatioClient.save(dto);
            LOGGER.info("Lisätty Varda-toimintamuoto organisaatiolle, oid: {}", organisaatio.oid);
        } else {
            LOGGER.info("Organisaatiolla {} on jo ennestään Varda-toimintamuoto.", organisaatio.oid);
        }
    }
}
