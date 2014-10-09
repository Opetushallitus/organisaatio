/*
 * Copyright (c) 2014 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 */
package fi.vm.sade.organisaatio.business.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioKoodistoException;
import fi.vm.sade.organisaatio.model.Organisaatio;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Päivittää organisaatiopalvelussa lisätyn tai muokatun organisaation tiedot
 * koodistoon.
 *
 */
@Component
public class OrganisaatioKoodisto {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrganisaatioKoodistoClient client;

    private final Gson gson;

    private final static String INFO_CODE_SAVE_FAILED = "organisaatio.koodisto.tallennusvirhe";

    private boolean reauthorize;

    /**
     * Luo instanssin ja alustaa gson:in
     */
    public OrganisaatioKoodisto() {
        gson = new GsonBuilder().create();
    }

    private OrganisaatioKoodistoClient getClient() {
        if (client == null) {
            client = new OrganisaatioKoodistoClient();
        }
        client.setReauthorize(reauthorize);
        return client;
    }

    /**
     * Hakee koodin koodistosta
     *
     * @param koodistoUri Koodisto URI
     * @param tunniste URI-spesifinen tunniste (toimipistekoodi,
     * oppilaitosnumero tai y-tunnus ilman väliviivaa)
     * @return Koodiobjekti tai null jos ei löytynyt
     */
    private OrganisaatioKoodistoKoodi haeKoodi(String koodistoUri, String tunniste) {
        String json = getClient().get("/rest/codeelement/" + koodistoUri + "_" + tunniste + "/1");
        LOG.debug("Haettiin koodi: " + (json == null ? null : json.toString()));
        return (json == null ? null : gson.fromJson(json, OrganisaatioKoodistoKoodi.class));
    }

    // Päivittää koodistoon olemassaolevan koodin (PUT)
    private boolean paivitaKoodi(OrganisaatioKoodistoKoodi koodi) {
        try {
            getClient().put(gson.toJson(koodi));
        } catch (OrganisaatioKoodistoException e) {
            LOG.debug("Koodi update failed. Reason: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Lisää koodistoon uuden koodin (POST)
     *
     * @param koodi Lisättävä koodi
     * @param uri Koodisto URI ("opetuspisteet", "oppilaitosnumero",
     * "koulutustoimija" tai "yhteishaunkoulukoodi")
     * @return true jos lisääminen onnistui, false muuten
     */
    private boolean lisaaKoodi(OrganisaatioKoodistoKoodi koodi, String uri) {
        try {
            getClient().post(gson.toJson(koodi), uri);
        } catch (OrganisaatioKoodistoException e) {
            LOG.warn("Koodi insert failed. Reason: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Lisää koodistoon uuden koodin koodiarvolla <uri>_<tunniste>
     *
     * @param uri KoodiURI
     * @param tunniste Uri-kohtainen tunniste
     * @param entity Organisaatio jolle koodi lisätään
     * @return True jos onnistui, false jos lisääminen ei onnistunut
     */
    private boolean luoKoodi(String uri, String tunniste, Organisaatio entity) {
        // Lisätään koodi
        OrganisaatioKoodistoKoodi uk = new OrganisaatioKoodistoKoodi();

        uk.setKoodiArvo(tunniste);
        uk.setVoimassaAlkuPvm(new SimpleDateFormat("yyyy-MM-dd").format(entity.getAlkuPvm()));
        uk.setVersio(1);
        uk.setTila("LUONNOS");

        for (String lang : entity.getNimi().getValues().keySet()) {
            OrganisaatioKoodistoKoodiMetadata umt = new OrganisaatioKoodistoKoodiMetadata();
            umt.setKieli(lang.toUpperCase());
            umt.setNimi(entity.getNimi().getString(lang));
            uk.getMetadata().add(umt);
        }
        return lisaaKoodi(uk, uri);
    }

    /**
     * Päivittää koodiston vastaamaan muokattua organisaatiota. Jos
     * organisaation nimeä tai lakkautuspäivämäärää on muutettu, koodistoon
     * päivitetään muuttuneet tiedot. Jos organisaatio on uusi ja koodia ei
     * löydy, luodaan uusi koodi. Päivitettävä koodi riippuu parametrina annetun
     * organisaation tyypistä seuraavasti: - Toimipiste (organisaatiolla on
     * toimipistekoodi muttei oppilaitoskoodia) => Päivitetään koodi
     * opetuspisteet_<toimipistekoodi>
     * - Oppilaitos (organisaatiolla on oppilaitoskoodi) => Päivitetään koodi
     * oppilaitosnumero_<oppilaitoskoodi>
     * - Koulutustoimija (organisaatiolla on y-tunnus) => Päivitetään koodi
     * koulutustoimija_<y-tunnus>
     * - Jos organisaatiolla on yhteishaunkoulukoodi => Päivitetään koodi
     * yhteishaunkoulukoodi_<yhteishaunkoulukoodi>
     *
     * @param entity Organisaatio
     * @param reauthorize Jos true, haetaan uusi tiketti, muuten haetaan vain jos ei jo ole
     *
     * @return true jos koodiston päivittäminen onnistui, false muuten
     */
    public String paivitaKoodisto(Organisaatio entity, boolean reauthorize) {
        /*
         * Koodiston koodit
         *     [0]: koodiUri
         *     [1]: tunniste
         */
        Object[][] koodiLista = {
            new Object[]{"opetuspisteet",
                (entity.getOppilaitosKoodi() != null && !entity.getOppilaitosKoodi().isEmpty()) ? null : entity.getToimipisteKoodi()},
            new Object[]{"oppilaitosnumero", entity.getOppilaitosKoodi()},
            new Object[]{"koulutustoimija", entity.getYtunnus()},
            new Object[]{"yhteishaunkoulukoodi", entity.getYhteishaunKoulukoodi()}
        };
        int URI_INDEX = 0;
        int TUNNISTE_INDEX = 1;

        this.reauthorize = reauthorize;

        for (Object[] koodiAlkio : koodiLista) {
            String uri = (String) koodiAlkio[URI_INDEX];
            String tunniste = (String) koodiAlkio[TUNNISTE_INDEX];
            OrganisaatioKoodistoKoodi koodi = null;
            LOG.debug("KOODI uri: " + uri + ", tunniste: '" + tunniste + "'");
            if (tunniste != null && !tunniste.isEmpty()) {
                // Tunniste on olemassa, haetaan koodistosta
                try {
                    koodi = haeKoodi(uri, tunniste);
                } catch (OrganisaatioKoodistoException e) {
                    LOG.warn("Koodin " + uri + "_" + tunniste + " hakeminen epäonnistui");
                    return INFO_CODE_SAVE_FAILED;
                }
                if (koodi == null) {
                    // Ei löytynyt, luodaan uusi
                    if (luoKoodi(uri, tunniste, entity) == true) {
                        LOG.info("Koodi " + uri + "_" + tunniste + " lisättiin");
                    } else {
                        LOG.warn("Koodin " + uri + "_" + tunniste + " lisääminen epäonnistui");
                        return INFO_CODE_SAVE_FAILED;
                    }
                } else {
                    // Päivitetään olemassa olevaan nimi ja lakkautuspvm jos muuttuneet
                    boolean muuttunut = false;

                    // Tarkistetaan onko lakkautuspäivämäärä muuttunut
                    if (entity.getLakkautusPvm() != null) {
                        String loppupvm = new SimpleDateFormat("yyyy-MM-dd").format(entity.getLakkautusPvm());
                        if (!loppupvm.equals(koodi.getVoimassaLoppuPvm())) {
                            LOG.debug("Lakkautuspvm muuttunut");
                            koodi.setVoimassaLoppuPvm(loppupvm);
                            muuttunut = true;
                        }
                    } else if (koodi.getVoimassaLoppuPvm() != null && !koodi.getVoimassaLoppuPvm().isEmpty()) {
                        LOG.debug("Lakkautuspvm poistettu");
                        koodi.setVoimassaLoppuPvm(null);
                        muuttunut = true;
                    }

                    // include-relaatiot tällä hetkellä koodistossa
                    Map<String, OrganisaatioKoodistoIncludesCodeElements> koodiIncludeUris = new HashMap<String, OrganisaatioKoodistoIncludesCodeElements>();
                    for (OrganisaatioKoodistoIncludesCodeElements ie : koodi.getIncludesCodeElements()) {
                        if (ie.getCodeElementUri().startsWith("posti_")) {
                            if (entity.getKayntiosoite() != null && entity.getKayntiosoite().getPostinumero() != null) {
                                String entityArvo = entity.getKayntiosoite().getPostinumero().split("#")[0];
                                if (!entityArvo.equals(ie.getCodeElementUri())) {
                                    LOG.debug("Postiosoite muuttunut");
                                    ie.setCodeElementUri(entityArvo);
                                    ie.setCodeElementVersion(1);
                                    muuttunut = true;
                                }
                            }
                        } else if (ie.getCodeElementUri().startsWith("kunta_")) {
                            if (entity.getKotipaikka() != null) {
                                String entityArvo = entity.getKotipaikka().split("#")[0];
                                if (!entityArvo.equals(ie.getCodeElementUri())) {
                                    LOG.debug("Kotipaikka muuttunut");
                                    ie.setCodeElementUri(entityArvo);
                                    ie.setCodeElementVersion(1);
                                    muuttunut = true;
                                }
                            }
                        } else if (ie.getCodeElementUri().startsWith("oppilaitostyyppi_")) {
                            if (entity.getOppilaitosTyyppi() != null) {
                                String entityArvo = entity.getOppilaitosTyyppi().split("#")[0];
                                if (!entityArvo.equals(ie.getCodeElementUri())) {
                                    LOG.debug("Oppilaitostyyppi muuttunut");
                                    ie.setCodeElementUri(entityArvo);
                                    ie.setCodeElementVersion(1);
                                    muuttunut = true;
                                }
                            }
                        } else if (ie.getCodeElementUri().startsWith("oppilaitoksenopetuskieli_")) {
                            if (entity.getKielet() != null) {
                                for (String kieli : entity.getKielet()) {
                                    String entityArvo = kieli.split("#")[0];
                                    if (!entityArvo.equals(ie.getCodeElementUri())) {
                                        LOG.debug("Oppilaitoksenopetuskieli muuttunut");
                                        ie.setCodeElementUri(entityArvo);
                                        ie.setCodeElementVersion(1);
                                        muuttunut = true;
                                    }
                                }
                            }
                        }
                        LOG.debug("PUT " + ie.getCodeElementUri());
                        koodiIncludeUris.put(ie.getCodeElementUri(), ie);
                    }
                    // Tarkistetaan pitääkö tallentaa uusia include-relaatioita
                    if (entity.getKayntiosoite()!=null && entity.getKayntiosoite().getPostinumero()!=null &&
                            !entity.getKayntiosoite().getPostinumero().isEmpty()) {
                        if (!koodiIncludeUris.containsKey(entity.getKayntiosoite().getPostinumero().split("#")[0])) {
                            LOG.debug("!CONTAINS: " + entity.getKayntiosoite().getPostinumero().split("#")[0]);
                            LOG.debug("Postiosoite lisätty");
                            OrganisaatioKoodistoIncludesCodeElements ie = new OrganisaatioKoodistoIncludesCodeElements();
                            ie.setCodeElementUri(entity.getKayntiosoite().getPostinumero().split("#")[0]);
                            ie.setCodeElementVersion(1);
                            koodi.getIncludesCodeElements().add(ie);
                            muuttunut = true;
                        }
                    }
                    if (entity.getKotipaikka()!=null && !entity.getKotipaikka().isEmpty()) {
                        if (!koodiIncludeUris.containsKey(entity.getKotipaikka().split("#")[0])) {
                            LOG.debug("!CONTAINS: " + entity.getKotipaikka());
                            LOG.debug("Kotipaikka lisätty");
                            OrganisaatioKoodistoIncludesCodeElements ie = new OrganisaatioKoodistoIncludesCodeElements();
                            ie.setCodeElementUri(entity.getKotipaikka().split("#")[0]);
                            ie.setCodeElementVersion(1);
                            koodi.getIncludesCodeElements().add(ie);
                            muuttunut = true;
                        }
                    }
                    if (entity.getOppilaitosTyyppi()!=null && !entity.getOppilaitosTyyppi().isEmpty()) {
                        if (!koodiIncludeUris.containsKey(entity.getOppilaitosTyyppi().split("#")[0])) {
                            LOG.debug("!CONTAINS: " + entity.getOppilaitosTyyppi());
                            LOG.debug("Oppilaitostyyppi lisätty");
                            OrganisaatioKoodistoIncludesCodeElements ie = new OrganisaatioKoodistoIncludesCodeElements();
                            ie.setCodeElementUri(entity.getOppilaitosTyyppi().split("#")[0]);
                            //ie.setCodeElementValue(entity.getOppilaitosTyyppi().split("#")[0].split("oppilaitostyyppi_")[1]);
                            ie.setCodeElementVersion(1);
                            koodi.getIncludesCodeElements().add(ie);
                            muuttunut = true;
                        }
                    }
                    if (entity.getKielet()!=null && !entity.getKielet().isEmpty()) {
                        for (String kieli : entity.getKielet()) {
                            if (!koodiIncludeUris.containsKey(kieli.split("#")[0])) {
                                LOG.debug("!CONTAINS: " + kieli);
                                LOG.debug("Oppilaitoksenopetuskieli lisätty");
                                OrganisaatioKoodistoIncludesCodeElements ie = new OrganisaatioKoodistoIncludesCodeElements();
                                ie.setCodeElementUri(kieli.split("#")[0]);
                                ie.setCodeElementVersion(1);
                                koodi.getIncludesCodeElements().add(ie);
                                muuttunut = true;
                            }
                        }
                    }

                    // Koodin nimet tällä hetkellä koodistossa
                    Map<String, OrganisaatioKoodistoKoodiMetadata> koodiNyt = new HashMap<String, OrganisaatioKoodistoKoodiMetadata>();
                    for (OrganisaatioKoodistoKoodiMetadata km : koodi.getMetadata()) {
                        koodiNyt.put(km.getKieli(), km);
                    }

                    // Tarkistetaan onko nimi muuttunut
                    for (String kieli : entity.getNimi().getValues().keySet()) {
                        String uusiNimi = entity.getNimi().getValues().get(kieli);
                        LOG.debug("Nimi: " + kieli.toUpperCase() + "=" + uusiNimi);

                        OrganisaatioKoodistoKoodiMetadata km = koodiNyt.get(kieli.toUpperCase());
                        if (km != null) {
                            // Kieliversio on olemassa
                            if (!km.getNimi().equals(uusiNimi)) {
                                LOG.debug("Nimi(" + kieli + ") muuttunut");
                                km.setNimi(uusiNimi);
                                // Olemassaolevia kenttiä lyhytNimi ja kuvaus ei ylikirjoiteta
                                muuttunut = true;
                            } else {
                                LOG.debug("Nimi(" + kieli + ") ei ole muuttunut");
                            }
                            koodiNyt.remove(kieli.toUpperCase());
                        } else {
                            // Lisää koodiin
                            OrganisaatioKoodistoKoodiMetadata ukm = new OrganisaatioKoodistoKoodiMetadata();
                            ukm.setKieli(kieli.toUpperCase());
                            ukm.setNimi(uusiNimi);
                            koodi.getMetadata().add(ukm);
                            LOG.debug("Nimi(" + kieli + ") lisätty");
                            muuttunut = true;
                        }
                    }
                    // Poistetaan koodistakin kielet jotka on poistettu organisaatiossa
                    for (String kieli : koodiNyt.keySet()) {
                        koodi.getMetadata().remove(koodiNyt.get(kieli));
                        LOG.debug("Nimi(" + kieli + ") poistettu");
                        muuttunut = true;
                    }

                    if (muuttunut == true) {
                        if (paivitaKoodi(koodi) == true) {
                            LOG.info("Koodi " + uri + "_" + tunniste + " päivitettiin");
                        } else {
                            LOG.warn("Koodin " + uri + "_" + tunniste + " päivitys epäonnistui");
                            return INFO_CODE_SAVE_FAILED;
                        }
                    } else {
                        LOG.debug("Ei muutoksia");
                    }
                }
            }
        }
        return null;
    }
}
