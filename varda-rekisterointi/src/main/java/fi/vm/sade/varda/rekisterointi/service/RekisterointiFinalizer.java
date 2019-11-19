package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.client.OrganisaatioClient;
import fi.vm.sade.varda.rekisterointi.model.Organisaatio;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioV4Dto;
import fi.vm.sade.varda.rekisterointi.model.Paatos;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RekisterointiFinalizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RekisterointiFinalizer.class);

    private final OrganisaatioService organisaatioService;
    private final OrganisaatioClient organisaatioClient;

    public RekisterointiFinalizer(OrganisaatioService organisaatioService,
                                  OrganisaatioClient organisaatioClient) {
        this.organisaatioService = organisaatioService;
        this.organisaatioClient = organisaatioClient;
    }

    public void finalize(Rekisterointi rekisterointi, Paatos paatos) {
        Organisaatio organisaatio = rekisterointi.organisaatio;
        if (organisaatio.oid != null) {
            paivitaOrganisaatioTyyppi(organisaatio);
        } else {
            luoOrganisaatio(organisaatio);
        }
    }

    String luoOrganisaatio(Organisaatio organisaatio) {
        OrganisaatioV4Dto dto = organisaatioService.muunnaOrganisaatio(organisaatio);
        OrganisaatioV4Dto luotu = organisaatioClient.create(dto);
        LOGGER.info("Luotu rekisteröinnin pohjalta organisaatio: {}", luotu.oid);
        return luotu.oid;
    }

    void paivitaOrganisaatioTyyppi(Organisaatio organisaatio) {
        // TODO: päivitä organisaatiotyyppi, jos ei varhaiskasvatuksen järjestäjä
    }
}
