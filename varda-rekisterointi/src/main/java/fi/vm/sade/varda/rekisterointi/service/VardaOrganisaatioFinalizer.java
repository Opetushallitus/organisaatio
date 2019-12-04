package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.client.OrganisaatioClient;
import fi.vm.sade.varda.rekisterointi.exception.InvalidInputException;
import fi.vm.sade.varda.rekisterointi.model.Organisaatio;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioV4Dto;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class VardaOrganisaatioFinalizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(VardaOrganisaatioFinalizer.class);
    private static final String VARDA_ORGANISAATIOTYYPPI = "organisaatiotyyppi_07";

    private final OrganisaatioService organisaatioService;
    private final OrganisaatioClient organisaatioClient;

    @Transactional
    public String luoTaiPaivitaOrganisaatio(Rekisterointi rekisterointi) {
        Organisaatio organisaatio = rekisterointi.organisaatio;
        String oid = organisaatio.oid;
        if (oid != null) {
            paivitaVardaTiedot(oid);
        } else {
            oid = luoOrganisaatio(organisaatio);
        }
        return oid;
    }

    private String luoOrganisaatio(Organisaatio organisaatio) {
        OrganisaatioV4Dto dto = organisaatioService.muunnaOrganisaatio(organisaatio);
        OrganisaatioV4Dto luotu = organisaatioClient.create(dto);
        LOGGER.info("Luotu uusi organisaatio {} rekisteröinnin pohjalta.", luotu.oid);
        return luotu.oid;
    }

    private void paivitaVardaTiedot(String organisaatioOid) {
        OrganisaatioV4Dto dto = organisaatioClient.getV4ByOid(organisaatioOid).orElseThrow(
                () -> new InvalidInputException("Organisaatiota ei löydy, oid: " + organisaatioOid)
        );
        if (!dto.tyypit.contains(VARDA_ORGANISAATIOTYYPPI)) {
            dto.tyypit = new HashSet<>(dto.tyypit);
            dto.tyypit.add(VARDA_ORGANISAATIOTYYPPI);
            organisaatioClient.save(dto);
            LOGGER.info("Lisätty varhaiskasvatuksen organisaatiotyyppi organisaatiolle, oid: {}", organisaatioOid);
        } else {
            LOGGER.debug("Organisaatiolla {} on jo ennestään varhaiskasvatuksen organisaatiotyyppi.", organisaatioOid);
        }
    }

}
