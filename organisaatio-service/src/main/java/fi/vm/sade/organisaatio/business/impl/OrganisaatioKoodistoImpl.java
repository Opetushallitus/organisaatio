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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.vm.sade.koodisto.service.types.common.KoodiType;
import fi.vm.sade.organisaatio.business.OrganisaatioKoodisto;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioKoodistoException;
import fi.vm.sade.organisaatio.config.UrlConfiguration;
import fi.vm.sade.organisaatio.model.Organisaatio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Päivittää organisaatiopalvelussa lisätyn tai muokatun organisaation tiedot
 * koodistoon.
 *
 */
@Component
@Primary
public class OrganisaatioKoodistoImpl implements OrganisaatioKoodisto {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final OrganisaatioKoodistoClient client;

    private final Gson gson;

    private final ObjectMapper objectMapper;

    private final UrlConfiguration urlConfiguration;

    private final static String INFO_CODE_SAVE_FAILED = "organisaatio.koodisto.tallennusvirhe";

    private boolean reauthorize;

    /**
     * Luo instanssin ja alustaa gson:in
     */
    @Autowired
    public OrganisaatioKoodistoImpl(OrganisaatioKoodistoClient client,
                                    UrlConfiguration urlConfiguration,
                                    ObjectMapper objectMapper) {
        this.client = client;
        this.urlConfiguration = urlConfiguration;
        this.objectMapper = objectMapper;
        gson = new GsonBuilder().create();
    }

    private OrganisaatioKoodistoClient getClient() {
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
        tunniste = tunniste.replace("-", "");
        String url = this.urlConfiguration.url("organisaatio-service.koodisto-service.koodi.v1", koodistoUri, tunniste);
        String json = getClient().get(url);
        LOG.debug("Haettiin koodi: " + (json));
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
     * @return Luotu koodi jos onnistui, null jos lisääminen ei onnistunut
     */
    private OrganisaatioKoodistoKoodi luoKoodi(String uri, String tunniste, Organisaatio entity) {
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
        if (lisaaKoodi(uk, uri)) {
            try {
                uk = haeKoodi(uri, tunniste);
            } catch (OrganisaatioKoodistoException e) {
                LOG.warn("Koodin " + uri + "_" + tunniste + " hakeminen epäonnistui");
                return null;
            }
            return uk;
        }
        return null;
    }

    /**
     * Päivittää elements-parametriin relaatiot organisaation entityn perusteella
     *
     * @param entityRelaatiot Organisaation entityssä olevat relaatiot
     * @param elements Koodin nykyiset relaatiot.
     * @return true jos elements-listaa päivitettiin, false muuten
     */
    protected boolean paivitaCodeElements(List<String> entityRelaatiot, List<OrganisaatioKoodistoKoodiCodeElements> elements) {
        boolean muuttunut = false;

        // Tehdään prefix-lista korvattavien relaatioiden vertailua varten
        Map<String, Object> entityRelaatiotPrefixlist = new HashMap<>();
        for (String rel : entityRelaatiot) {
            entityRelaatiotPrefixlist.put(rel.split("_")[0], null);
        }


        // Listaa kaikki koodiston nykyiset relaatiot
        Map<String, OrganisaatioKoodistoKoodiCodeElements> koodistoRelaatiot = new HashMap<>();
        for (OrganisaatioKoodistoKoodiCodeElements ie : elements) {
            koodistoRelaatiot.put(ie.getCodeElementUri() + "#" + ie.getCodeElementVersion(), ie);
        }

        // Poista koodistosta kaikki ne relaatiot, joita ei ole enää organisaatiossa
        for (String kRel : koodistoRelaatiot.keySet()) {
            if (entityRelaatiotPrefixlist.containsKey(kRel.split("_")[0])) {
                OrganisaatioKoodistoKoodiCodeElements e = koodistoRelaatiot.get(kRel);
                if (e != null && !e.isPassive()) {
                    LOG.debug("Remove includesCodeElements relation: " + kRel);
                    elements.remove(e);
                    muuttunut = true;
                }
            }
        }
        // Lisää koodistoon ne organisaation relaatiot joita siellä ei vielä ole
        for (String oRel : entityRelaatiot) {
            if (oRel!=null) {
                LOG.debug("Add includesCodeElements relation: " + oRel);
                OrganisaatioKoodistoKoodiCodeElements e = new OrganisaatioKoodistoKoodiCodeElements();
                e.setCodeElementUri(oRel.split("#")[0]);
                int versio = 1;
                try {
                    if (oRel.split("#").length == 2) {
                        versio = Integer.parseInt(oRel.split("#")[1]);
                    }
                } catch (NumberFormatException ex) {
                }
                e.setCodeElementVersion(versio);
                elements.add(e);
                muuttunut = true;
            }
        }
        return muuttunut;
    }

    // Päivittää Oppilaitoksen sisältyy-relaatiot
    private boolean paivitaIncludesCodeElements(Organisaatio entity, OrganisaatioKoodistoKoodi koodi) {
        List<String> entityRelaatiot = new ArrayList<>();
        if (koodi.getKoodiUri().startsWith("oppilaitosnumero_")) {
            if (entity.getPostiosoite() != null && entity.getPostiosoite().getPostinumero() != null &&
                    !entity.getPostiosoite().getPostinumero().startsWith("posti_00000")) {
                entityRelaatiot.add(entity.getPostiosoite().getPostinumero());
            }
            entityRelaatiot.add(entity.getKotipaikka());
            entityRelaatiot.add(entity.getOppilaitosTyyppi());
            // childien opetuspisteet
            if (entity.getOppilaitosKoodi()!=null) {
                // Käydään läpi aliorganisaatiot, jotka eivät ole passiivisia
                for (Organisaatio child : entity.getChildren(false)) {
                    if (child.getOpetuspisteenJarjNro()!=null) {
                        entityRelaatiot.add("opetuspisteet_" + entity.getOppilaitosKoodi() + child.getOpetuspisteenJarjNro());
                    }
                }
            }
            for (String kieli : entity.getKielet()) {
                entityRelaatiot.add(kieli);
            }
        }
        return paivitaCodeElements(entityRelaatiot, koodi.getIncludesCodeElements());
    }

    // Päivittää opetuspisteen sisältyy-relaatiot
    private boolean paivitaWithinCodeElements(Organisaatio entity, OrganisaatioKoodistoKoodi koodi) {
        // Listaa kaikki organisaation relaatiot
        List<String> entityRelaatiot = new ArrayList<>();
        if (koodi.getKoodiUri().startsWith("opetuspisteet_")) {
            // parentin oppilaitosnumero
            Organisaatio parent = entity.getParent();
            if (parent !=null && parent.getOppilaitosKoodi()!=null) {
                entityRelaatiot.add("oppilaitosnumero_" + parent.getOppilaitosKoodi());
            }
        }
        return paivitaCodeElements(entityRelaatiot, koodi.getWithinCodeElements());
    }

    /**
     * Päivittää koodiston vastaamaan muokattua organisaatiota. (kts.
     * {@link #paivitaKoodisto(fi.vm.sade.organisaatio.model.Organisaatio, boolean)}.
     *
     * @param entity organisaatio
     * @param reauthorize jos true, haetaan uusi tiketti, muuten haetaan vain jos ei jo ole
     */
    @Override
    @Async
    public void paivitaKoodistoAsync(Organisaatio entity, boolean reauthorize) {
        String virheviesti = paivitaKoodisto(entity, reauthorize);
        if (virheviesti != null) {
            LOG.error("Organisaation päivittäminen koodistoon epäonnistui: {}", virheviesti);
        }
    }

    /**
     * Päivittää koodiston vastaamaan muokattua organisaatiota.
     * 1. Jos organisaatio on uusi ja koodia ei löydy
     *    => Luodaan uusi koodi
     * 2. Jos organisaation nimeä tai voimassaoloaikaa on muutettu
     *    => Koodistoon päivitetään muuttuneet tiedot.
     *
     * Päivitettävä koodi riippuu parametrina annetun organisaation tyypistä:
     * - Toimipiste (organisaatiolla on toimipistekoodi muttei oppilaitoskoodia)
     *   => Päivitetään koodi: opetuspisteet_[toimipistekoodi]
     * - Oppilaitos (organisaatiolla on oppilaitoskoodi)
     *   => Päivitetään koodi: oppilaitosnumero_[oppilaitoskoodi]
     * - Koulutustoimija (organisaatiolla on y-tunnus)
     *   => Päivitetään koodi: koulutustoimija_[y-tunnus]
     * - Jos organisaatiolla on yhteishaunkoulukoodi
     *   => Päivitetään koodi: yhteishaunkoulukoodi_[yhteishaunkoulukoodi]
     *
     * @param entity Organisaatio
     * @param reauthorize Jos true, haetaan uusi tiketti, muuten haetaan vain jos ei jo ole
     *
     * @return null jos koodiston päivittäminen onnistui, virheviesti jos epäonnistui
     */
    @Override
    public synchronized String paivitaKoodisto(Organisaatio entity, boolean reauthorize) {
        if (entity==null || entity.isOrganisaatioPoistettu()) {
            LOG.warn("Organiasaatiota ei voi päivittää koodistoon, organisaatio == null / poistettu");
            return null;
        }
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
                boolean muuttunut = false;
                if (koodi == null) {
                    // Ei löytynyt, luodaan uusi
                    koodi = luoKoodi(uri, tunniste, entity);
                    if (koodi != null) {
                        LOG.info("Koodi " + uri + "_" + tunniste + " lisättiin");
                        muuttunut = true;
                    } else {
                        LOG.warn("Koodin " + uri + "_" + tunniste + " lisääminen epäonnistui");
                        return INFO_CODE_SAVE_FAILED;
                    }
                } else {
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

                    // Tarkistetaan onko alkupäivämäärä muuttunut
                    if (entity.getAlkuPvm() != null) {
                        String alkupvm = new SimpleDateFormat("yyyy-MM-dd").format(entity.getAlkuPvm());
                        if (!alkupvm.equals(koodi.getVoimassaAlkuPvm())) {
                            LOG.debug("Alkupvm muuttunut");
                            koodi.setVoimassaAlkuPvm(alkupvm);
                            muuttunut = true;
                        }
                    } else if (koodi.getVoimassaAlkuPvm() != null && !koodi.getVoimassaAlkuPvm().isEmpty()) {
                        LOG.debug("Alkupvm poistettu");
                        koodi.setVoimassaAlkuPvm(null);
                        muuttunut = true;
                    }

                    // Koodin nimet tällä hetkellä koodistossa
                    Map<String, OrganisaatioKoodistoKoodiMetadata> koodiNyt = new HashMap<>();
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
                }

                muuttunut |= paivitaIncludesCodeElements(entity, koodi);
                muuttunut |= paivitaWithinCodeElements(entity, koodi);

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
        return null;
    }

    /**
     * Asettaa annetulle koodille lakkautuspäivämäärän.
     *
     * @param uri KoodiUri
     * @param tunniste Koodin tunniste, esim. opetuspiste koodille toimipistekoodi
     * @param lakkautusPvm Lakkautuspäivämäärä
     * @param reauthorize Jos true, haetaan uusi tiketti, muuten haetaan vain jos ei jo ole
     *
     * @return null jos koodiston päivittäminen onnistui, virheviesti jos epäonnistui
     */
    @Override
    public String lakkautaKoodi(String uri, String tunniste, Date lakkautusPvm, boolean reauthorize) {
        this.reauthorize = reauthorize;

        if (uri == null || uri.isEmpty()) {
            LOG.warn("Koodia ei voi lakkauttaa: uri == null / empty");
            return INFO_CODE_SAVE_FAILED;
        }

        if (tunniste == null || tunniste.isEmpty()) {
            LOG.warn("Koodia ei voi lakkauttaa: tunniste == null / empty");
            return INFO_CODE_SAVE_FAILED;
        }

        if (lakkautusPvm == null) {
            LOG.warn("Koodia ei voi lakkauttaa: lakkautusPvm == null / empty");
            return INFO_CODE_SAVE_FAILED;
        }

        OrganisaatioKoodistoKoodi koodi;
        LOG.debug("KOODI uri: " + uri + ", tunniste: '" + tunniste + "'");
        // Tunniste on olemassa, haetaan koodistosta
        try {
            koodi = haeKoodi(uri, tunniste);
        } catch (OrganisaatioKoodistoException e) {
            LOG.warn("Koodin " + uri + "_" + tunniste + " hakeminen epäonnistui");
            return INFO_CODE_SAVE_FAILED;
        }

        // Koodia ei löytynyt --> ei voida lakkauttaa
        if (koodi == null) {
            LOG.warn("Koodin " + uri + "_" + tunniste + " hakeminen epäonnistui, koodia ei löytynyt!");
            return INFO_CODE_SAVE_FAILED;
        }

        boolean muuttunut = false;

        // Tarkistetaan onko lakkautuspäivämäärä muuttunut
        String loppupvm = new SimpleDateFormat("yyyy-MM-dd").format(lakkautusPvm);
        if (!loppupvm.equals(koodi.getVoimassaLoppuPvm())) {
            LOG.debug("Lakkautuspvm muuttunut");
            koodi.setVoimassaLoppuPvm(loppupvm);
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
        return null;
    }

    @Override
    public Set<String> haeOppilaitoskoodit() {
        return this.haeKoodistonKoodit("oppilaitostyyppi");
    }

    private Set<String> haeKoodistonKoodit(String koodistoUri) {
        String url = this.urlConfiguration.url("organisaatio-service.koodisto-service.koodisto.koodit", koodistoUri);
        String json = this.client.get(url);
        CollectionType listType = objectMapper.getTypeFactory().
                constructCollectionType(List.class, KoodiType.class);
        List<KoodiType> koodiCollectionType;
        try {
            koodiCollectionType = this.objectMapper
                    .readerFor(listType)
                    .readValue(json);
        }
        catch (IOException ioe) {
            throw new RestClientException("Error while parsing koodisto return koodiValue for " + koodistoUri, ioe);
        }
        return koodiCollectionType.stream()
                .map(KoodiType::getKoodiUri)
                .collect(Collectors.toSet());
    }
}
