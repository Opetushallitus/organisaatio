package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.client.KayttooikeusClient;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Palvelu käyttäjän kutsumiseen.
 */
@Service
public class OrganisaatioKayttajaFinalizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganisaatioKayttajaFinalizer.class);
    static final String KAYTTOOIKEUSRYHMA_JOTPA_PROPERTY =
            "varda-rekisterointi.kayttooikeus.ryhma.jotpa";
    static final String KAYTTOOIKEUSRYHMA_PAIVAKOTI_PROPERTY =
            "varda-rekisterointi.kayttooikeus.ryhma.paivakoti";
    static final String KAYTTOOIKEUSRYHMA_PERHEPAIVAHOITAJA_PROPERTY =
            "varda-rekisterointi.kayttooikeus.ryhma.perhepaivahoitaja";
    static final String KAYTTOOIKEUSRYHMA_RYHMAPERHEPAIVAKOTI_PROPERTY =
            "varda-rekisterointi.kayttooikeus.ryhma.ryhmaperhepaivakoti";
    static final String VARDA_TOIMINTAMUOTO_PAIVAKOTI = "vardatoimintamuoto_tm01";
    private static final String VARDA_TOIMINTAMUOTO_PERHEPAIVAHOITO = "vardatoimintamuoto_tm02";
    private static final String VARDA_TOIMINTAMUOTO_RYHMAPERHEPAIVAHOITO = "vardatoimintamuoto_tm03";

    private final KayttooikeusClient kayttooikeusClient;
    private final Map<String,Long> toimintamuotoKayttooikeusRyhmaId;
    private final Long jotpaKayttooikeusRyhmaId;

    public OrganisaatioKayttajaFinalizer(KayttooikeusClient kayttooikeusClient,
                                  OphProperties properties) {
        this.kayttooikeusClient = kayttooikeusClient;
        toimintamuotoKayttooikeusRyhmaId = Map.of(
                VARDA_TOIMINTAMUOTO_PAIVAKOTI,
                Long.valueOf(properties.getProperty(KAYTTOOIKEUSRYHMA_PAIVAKOTI_PROPERTY)),
                VARDA_TOIMINTAMUOTO_PERHEPAIVAHOITO,
                Long.valueOf(properties.getProperty(KAYTTOOIKEUSRYHMA_PERHEPAIVAHOITAJA_PROPERTY)),
                VARDA_TOIMINTAMUOTO_RYHMAPERHEPAIVAHOITO,
                Long.valueOf(properties.getProperty(KAYTTOOIKEUSRYHMA_RYHMAPERHEPAIVAKOTI_PROPERTY))
        );
        jotpaKayttooikeusRyhmaId = Long.valueOf(properties.getProperty(KAYTTOOIKEUSRYHMA_JOTPA_PROPERTY));
    }

    /**
     * Kutsuu käyttäjän. Käyttäjälle annetaan rekisteröitävän toimijan toimintamuodon mukainen
     * käyttöoikeusryhmä.
     *
     * @param rekisterointi hyväksytty rekisteröinti, jolle kutsutaan käyttäjä.
     */
    void kutsuKayttaja(Rekisterointi rekisterointi) {
        kayttooikeusClient.kutsuKayttaja(
                rekisterointi.paatos.paattaja,
                rekisterointi.kayttaja,
                rekisterointi.organisaatio.oid,
                rekisterointi.tyyppi.equals("varda") ? kayttooikeusRyhmaId(rekisterointi.toimintamuoto) : jotpaKayttooikeusRyhmaId
        );
        LOGGER.info(
                "Kutsuttu käyttäjä {} organisaatioon {}.", rekisterointi.kayttaja.id, rekisterointi.organisaatio.oid);
    }

    private Long kayttooikeusRyhmaId(String toimintamuoto) {
        return toimintamuotoKayttooikeusRyhmaId.get(toimintamuoto);
    }
}
