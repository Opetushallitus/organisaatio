package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.client.OrganisaatioClient;
import fi.vm.sade.varda.rekisterointi.exception.InvalidInputException;
import fi.vm.sade.varda.rekisterointi.model.Organisaatio;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioDto;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class RekisterointiOrganisaatioFinalizer {

    static final String VARDA_ORGANISAATIOTYYPPI = "organisaatiotyyppi_07";
    static final String JOTPA_ORGANISAATIOTYYPPI = "organisaatiotyyppi_01";
    static final String REKISTEROINTITYYPPI_VARDA = "varda";
    static final String REKISTEROINTITYYPPI_JOTPA = "jotpa";
    private static final Logger LOGGER = LoggerFactory.getLogger(RekisterointiOrganisaatioFinalizer.class);

    private final OrganisaatioService organisaatioService;
    private final OrganisaatioClient organisaatioClient;

    /**
     * Luo tai päivitä organisaatio rekisteröinnin perusteella. Mikäli organisaatio on jo
     * olemassa, sille lisätään tarvittaessa varhaiskasvatuksen organisaatiotyyppi.
     *
     * @param rekisterointi hyväksytty rekisteröinti
     * @return organisaation OID.
     */
    @Transactional
    public String luoTaiPaivitaOrganisaatio(Rekisterointi rekisterointi) {
        Organisaatio organisaatio = rekisterointi.organisaatio;
        String oid = organisaatio.oid;
        if (oid != null) {
            LOGGER.info("Päivitetään organisaatiota: {}", oid);
            if (rekisterointi.tyyppi.equals(REKISTEROINTITYYPPI_VARDA)) {
                paivitaVardaTiedot(oid);
            } else if(rekisterointi.tyyppi.equals(REKISTEROINTITYYPPI_JOTPA)) {
                paivitaJotpaTiedot(oid);
            }
        } else {
            LOGGER.info("Luodaan organisaatio nimellä: {}", organisaatio.ytjNimi.nimi);
            oid = luoOrganisaatio(organisaatio);
        }
        return oid;
    }

    private String luoOrganisaatio(Organisaatio organisaatio) {
        OrganisaatioDto dto = organisaatioService.muunnaOrganisaatio(organisaatio);
        OrganisaatioDto luotu = organisaatioClient.create(dto);
        LOGGER.info("Luotu uusi organisaatio {} rekisteröinnin pohjalta.", luotu.oid);
        return luotu.oid;
    }

    private void paivitaVardaTiedot(String organisaatioOid) {
        OrganisaatioDto dto = organisaatioClient.getOrganisaatioByOid(organisaatioOid).orElseThrow(
                () -> new InvalidInputException("Organisaatiota ei löydy, oid: " + organisaatioOid)
        );
        if (!dto.tyypit.contains(VARDA_ORGANISAATIOTYYPPI)) {
            dto.tyypit = new HashSet<>(dto.tyypit);
            dto.tyypit.add(VARDA_ORGANISAATIOTYYPPI);
            dto.lakkautusPvm = null;
            organisaatioClient.save(dto);
            LOGGER.info("Lisätty varhaiskasvatuksen organisaatiotyyppi organisaatiolle, oid: {}", organisaatioOid);
        } else if (dto.lakkautusPvm != null) {
            dto.lakkautusPvm = null;
            organisaatioClient.save(dto);
            LOGGER.info("Organisaation lakkautuspäivämäärä poistettu, oid: {}", organisaatioOid);
        } else {
            LOGGER.debug("Organisaatioon ei tarvittu muutoksia, oid: {}", organisaatioOid);
        }
    }

    private void paivitaJotpaTiedot(String organisaatioOid) {
        OrganisaatioDto dto = organisaatioClient.getOrganisaatioByOid(organisaatioOid).orElseThrow(
                () -> new InvalidInputException("Organisaatiota ei löydy, oid: " + organisaatioOid)
        );
        OrganisaatioDto saved = null;
        if (!dto.tyypit.contains(JOTPA_ORGANISAATIOTYYPPI)) {
            dto.tyypit = new HashSet<>(dto.tyypit);
            dto.tyypit.add(JOTPA_ORGANISAATIOTYYPPI);
            dto.lakkautusPvm = null;
            saved = organisaatioClient.save(dto);
            LOGGER.info("Lisätty jotpa organisaatiotyyppi organisaatiolle, oid: {}", organisaatioOid);
        } else if (dto.lakkautusPvm != null) {
            dto.lakkautusPvm = null;
            saved = organisaatioClient.save(dto);
            LOGGER.info("Organisaation lakkautuspäivämäärä poistettu, oid: {}", organisaatioOid);
        } else {
            LOGGER.debug("Organisaatioon ei tarvittu muutoksia, oid: {}", organisaatioOid);
        }
        if (saved != null) {
            OrganisaatioDto jotpaOppilaitosDto = OrganisaatioDto.jotpaChildOppilaitosFrom(dto);
            OrganisaatioDto jotpaSaved = organisaatioClient.save(jotpaOppilaitosDto);
            LOGGER.info("Luotu oppilaitostyyppinen aliorganisaatio(oid: {}) Jotpa organisaatiolle(oid: {})", jotpaSaved.oid, organisaatioOid);
        }
    }
}
