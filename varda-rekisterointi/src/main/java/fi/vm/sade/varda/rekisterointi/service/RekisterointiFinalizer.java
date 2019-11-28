package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.client.KayttooikeusClient;
import fi.vm.sade.varda.rekisterointi.client.OrganisaatioClient;
import fi.vm.sade.varda.rekisterointi.exception.InvalidInputException;
import fi.vm.sade.varda.rekisterointi.model.Organisaatio;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioV4Dto;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;

@Service
public class RekisterointiFinalizer {

    static final String VARDA_ORGANISAATIOTYYPPI = "organisaatiotyyppi_07";
    static final String VARDA_TOIMINTAMUOTO_PAIVAKOTI = "vardatoimintamuoto_tm01";
    private static final String VARDA_TOIMINTAMUOTO_PERHEPAIVAHOITO = "vardatoimintamuoto_tm02";
    private static final String VARDA_TOIMINTAMUOTO_RYHMAPERHEPAIVAHOIO = "vardatoimintamuoto_tm03";
    private static final Logger LOGGER = LoggerFactory.getLogger(RekisterointiFinalizer.class);

    private final OrganisaatioService organisaatioService;
    private final OrganisaatioClient organisaatioClient;
    private final KayttooikeusClient kayttooikeusClient;
    private final Map<String,Long> toimintamuotoKayttooikeusRyhmaId;

    public RekisterointiFinalizer(OrganisaatioService organisaatioService,
                                  OrganisaatioClient organisaatioClient,
                                  KayttooikeusClient kayttooikeusClient,
                                  OphProperties properties) {
        this.organisaatioService = organisaatioService;
        this.organisaatioClient = organisaatioClient;
        this.kayttooikeusClient = kayttooikeusClient;
        toimintamuotoKayttooikeusRyhmaId = Map.of(
                VARDA_TOIMINTAMUOTO_PAIVAKOTI,
                Long.valueOf(properties.getProperty("varda-rekisterointi.kayttooikeus.ryhma.paivakoti")),
                VARDA_TOIMINTAMUOTO_PERHEPAIVAHOITO,
                Long.valueOf(properties.getProperty("varda-rekisterointi.kayttooikeus.ryhma.perhepaivahoito")),
                VARDA_TOIMINTAMUOTO_RYHMAPERHEPAIVAHOIO,
                Long.valueOf(properties.getProperty("varda-rekisterointi.kayttooikeus.ryhma.ryhmaperhepaivahoito"))
        );
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void finalize(Rekisterointi rekisterointi, String paattajaOid) {
        Organisaatio organisaatio = rekisterointi.organisaatio;
        if (organisaatio.oid != null) {
            paivitaVardaTiedot(rekisterointi);
        } else {
            String oid = luoOrganisaatio(organisaatio, rekisterointi);
            kayttooikeusClient.kutsuKayttaja(
                    paattajaOid,
                    rekisterointi.kayttaja,
                    oid,
                    kayttooikeusRyhmaId(rekisterointi.toimintamuoto));
        }

    }

    private String luoOrganisaatio(Organisaatio organisaatio, Rekisterointi rekisterointi) {
        OrganisaatioV4Dto dto = organisaatioService.muunnaOrganisaatio(organisaatio);
        dto.piilotettu = piilotettavaToimintamuoto(rekisterointi.toimintamuoto);
        OrganisaatioV4Dto luotu = organisaatioClient.create(dto);
        LOGGER.info("Luotu uusi organisaatio {} rekisteröinnin pohjalta.", luotu.oid);
        return luotu.oid;
    }

    private void paivitaVardaTiedot(Rekisterointi rekisterointi) {
        String oid = rekisterointi.organisaatio.oid;
        OrganisaatioV4Dto dto = organisaatioClient.getV4ByOid(oid).orElseThrow(
                () -> new InvalidInputException("Organisaatiota ei löydy, oid: " + oid)
        );
        if (!dto.tyypit.contains(VARDA_ORGANISAATIOTYYPPI)) {
            dto.tyypit = new HashSet<>(dto.tyypit);
            dto.tyypit.add(VARDA_ORGANISAATIOTYYPPI);
            LOGGER.info("Lisätty varhaiskasvatuksen organisaatiotyyppi organisaatiolle, oid: {}", oid);
        } else {
            LOGGER.debug("Organisaatiolla {} on jo ennestään varhaiskasvatuksen organisaatiotyyppi.", oid);
        }
        dto.piilotettu = piilotettavaToimintamuoto(rekisterointi.toimintamuoto);
        organisaatioClient.save(dto);
    }

    private boolean piilotettavaToimintamuoto(String toimintamuoto) {
        return !VARDA_TOIMINTAMUOTO_PAIVAKOTI.equals(toimintamuoto);
    }

    private Long kayttooikeusRyhmaId(String toimintamuoto) {
        return toimintamuotoKayttooikeusRyhmaId.get(toimintamuoto);
    }
}
