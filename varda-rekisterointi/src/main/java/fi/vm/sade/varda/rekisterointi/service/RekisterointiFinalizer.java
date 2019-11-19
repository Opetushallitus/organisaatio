package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.client.OrganisaatioClient;
import fi.vm.sade.varda.rekisterointi.exception.InvalidInputException;
import fi.vm.sade.varda.rekisterointi.model.Organisaatio;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioV4Dto;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
@Transactional
public class RekisterointiFinalizer {

    static final String VARDA_ORGANISAATIOTYYPPI = "organisaatiotyyppi_07";
    static final String JULKINEN_VARDA_TOIMINTAMUOTO = "vardatoimintamuoto_tm01";
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
            paivitaVardaTiedot(rekisterointi);
        } else {
            luoOrganisaatio(organisaatio, rekisterointi);
        }
    }

    void luoOrganisaatio(Organisaatio organisaatio, Rekisterointi rekisterointi) {
        OrganisaatioV4Dto dto = organisaatioService.muunnaOrganisaatio(organisaatio);
        dto.piilotettu = piilotettavaToimintamuoto(rekisterointi.toimintamuoto);
        organisaatioClient.create(dto);
        LOGGER.info("Luotu uusi organisaatio rekisteröinnin pohjalta.");
    }

    void paivitaVardaTiedot(Rekisterointi rekisterointi) {
        String oid = rekisterointi.organisaatio.oid;
        OrganisaatioV4Dto dto = organisaatioClient.getV4ByOid(oid).orElseThrow(
                () -> new InvalidInputException("Organisaatiota ei löydy, oid: " + oid)
        );
        if (!dto.tyypit.contains(VARDA_ORGANISAATIOTYYPPI)) {
            dto.tyypit = new HashSet<>(dto.tyypit);
            dto.tyypit.add(VARDA_ORGANISAATIOTYYPPI);
            LOGGER.info("Lisätty Varda-toimintamuoto organisaatiolle, oid: {}", oid);
        } else {
            LOGGER.info("Organisaatiolla {} on jo ennestään Varda-toimintamuoto.", oid);
        }
        dto.piilotettu = piilotettavaToimintamuoto(rekisterointi.toimintamuoto);
        organisaatioClient.save(dto);
    }

    boolean piilotettavaToimintamuoto(String toimintamuoto) {
        return !JULKINEN_VARDA_TOIMINTAMUOTO.equals(toimintamuoto);
    }
}
