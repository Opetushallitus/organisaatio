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

package fi.vm.sade.organisaatio.util;

import fi.vm.sade.organisaatio.api.model.types.OsoiteTyyppi;
import fi.vm.sade.organisaatio.api.model.types.PuhelinNumeroTyyppi;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioNimiRDTO;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;

import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author simok
 */
public final class OrganisaatioRDTOTestUtil {

    public static final Map<String, String> DEFAULT_KAYNTIOSOITE = createOsoite(OsoiteTyyppi.KAYNTI, "Kayntiosoite 2B", "posti_00550", "Helsinki");
    public static final Map<String, String> DEFAULT_POSTIOSOITE = createOsoite(OsoiteTyyppi.POSTI, "Postiosoite 1A", "00550", "Helsinki");
    public static final Map<String, String> DEFAULT_WWW = createWww("http://test.oph.fi");
    public static final Map<String, String> DEFAULT_EMAIL = createEmail("asd@asd.asd");
    public static final Map<String, String> DEFAULT_PUHELIN =  createPuhelin(PuhelinNumeroTyyppi.PUHELIN, "123");
    public static final Set<String> DEFAULT_KIELET = Collections.singleton("oppilaitoksenopetuskieli_1#1");
    public static final Date DEFAULT_VOIMASSAOLO_ALKU = createPvm(0);
    public static final String DEFAULT_KOTIPAIKKA = "kunta_092";
    public static final String DEFAULT_MAA = "maatjavaltiot1_fin";

    public static final String OPH_OID = "1.2.246.562.24.00000000001";

    private OrganisaatioRDTOTestUtil() {

    }

    public static Map<String, String> createOsoite(OsoiteTyyppi osoiteTyyppi, String osoite, String postinumeroUri, String postitoimipaikka) {
        Map<String, String> result = new HashMap<>();

        result.put("kieli", "kieli_fi#1");
        result.put("osoiteTyyppi", osoiteTyyppi.value());
        result.put("osoite", osoite);
        result.put("postinumeroUri", postinumeroUri);
        result.put("postitoimipaikka", postitoimipaikka);

        return result;
    }

    public static Map<String, String> createPuhelin(PuhelinNumeroTyyppi tyyppi, String puhNro) {
        Map<String, String> result = new HashMap<>();

        result.put("kieli", "kieli_fi#1");
        result.put("numero", puhNro);
        result.put("tyyppi", tyyppi.value());

        return result;
    }

    public static Map<String, String> createEmail(String emailOsoite) {
        Map<String, String> result = new HashMap<>();

        result.put("kieli", "kieli_fi#1");
        result.put("email", emailOsoite);

        return result;
    }

    public static Map<String, String> createWww(String wwwOsoite) {
        Map<String, String> result = new HashMap<>();

        result.put("kieli", "kieli_fi#1");
        result.put("www", wwwOsoite);

        return result;
    }

    public static OrganisaatioNimiRDTO createNimi(String nimi, Date alkuPvm) {
        OrganisaatioNimiRDTO nimiRDTO = new OrganisaatioNimiRDTO();

        Map<String, String> nimiMkt = new HashMap<>();
        nimiMkt.put("fi", nimi);

        nimiRDTO.setNimi(nimiMkt);
        if (alkuPvm != null) {
            nimiRDTO.setAlkuPvm(alkuPvm);
        }
        else {
            nimiRDTO.setAlkuPvm(new Date());
        }

        return nimiRDTO;
    }

    private static Date createPvm(int n) {
        return new GregorianCalendar(2000+n, 0, 0).getTime();
    }

    public static OrganisaatioRDTO createOrganisaatio(String nimi,
                                                      String organisaatioTyyppi,
                                                      String parentOid, boolean nullifyOid) {
        return createOrganisaatio(nimi, organisaatioTyyppi, null, parentOid, nullifyOid);
    }

    public static OrganisaatioRDTOV4 createOrganisaatioV4(String nimi,
                                                      String organisaatioTyyppi,
                                                      OrganisaatioRDTOV4 parent, boolean nullifyOid) {
        String parentOid = OPH_OID;
        if (parent != null) {
            parentOid = parent.getOid();
        }
        return createOrganisaatioV4(nimi, organisaatioTyyppi, null, parentOid, nullifyOid);
    }

    /**
     * Luodaan organisaatio, jolla on asetettu kaikki organisaatiolle pakolliset kentät.
     * Organisaatiolle pakollisia tietoja ovat:
     * nimi, tyyppi, alkuPvm, kotipaikka, maa, kieli, postiosoite
     *
     * @param nimi
     * @param organisaatioTyyppi
     * @param oid
     * @param parentOid
     * @return
     */
    public static OrganisaatioRDTO createOrganisaatio(String nimi,
                                                      String organisaatioTyyppi,
                                                      String oid,
                                                      String parentOid, boolean nullifyOid) {
        OrganisaatioRDTO organisaatio = new OrganisaatioRDTO();
        OrganisaatioNimiRDTO nimiRDTO = createNimi(nimi, null);

        organisaatio.setNimi(nimiRDTO.getNimi());
        List<OrganisaatioNimiRDTO> nimet = new ArrayList<>();
        nimet.add(nimiRDTO);
        organisaatio.setNimet(nimet);

        if (nullifyOid) {
            organisaatio.setOid(null);
        }
        else if (isNullOrEmpty(oid)) {
            organisaatio.setOid(OrganisaatioOidTestUtil.createOid());
        }
        else {
            organisaatio.setOid(oid);
        }

        organisaatio.setParentOid(parentOid);

        if (organisaatioTyyppi != null) {
            organisaatio.getTyypit().add(organisaatioTyyppi);
        }

        organisaatio.setAlkuPvm(DEFAULT_VOIMASSAOLO_ALKU);
        organisaatio.setKotipaikkaUri(DEFAULT_KOTIPAIKKA);
        organisaatio.setMaaUri(DEFAULT_MAA);
        organisaatio.setKieletUris(DEFAULT_KIELET);

        organisaatio.getYhteystiedot().add(DEFAULT_POSTIOSOITE);
        organisaatio.getYhteystiedot().add(DEFAULT_KAYNTIOSOITE);
        organisaatio.getYhteystiedot().add(DEFAULT_PUHELIN);
        organisaatio.getYhteystiedot().add(DEFAULT_WWW);
        organisaatio.getYhteystiedot().add(DEFAULT_EMAIL);

        return organisaatio;
    }
    public static OrganisaatioRDTOV4 createOrganisaatioV4(String nimi,
                                                      String organisaatioTyyppi,
                                                      String oid,
                                                      String parentOid, boolean nullifyOid) {
        OrganisaatioRDTOV4 organisaatio = new OrganisaatioRDTOV4();
        OrganisaatioNimiRDTO nimiRDTO = createNimi(nimi, null);

        organisaatio.setNimi(nimiRDTO.getNimi());
        List<OrganisaatioNimiRDTO> nimet = new ArrayList<>();
        nimet.add(nimiRDTO);
        organisaatio.setNimet(nimet);

        if (nullifyOid) {
            organisaatio.setOid(null);
        }
        else if (isNullOrEmpty(oid)) {
            organisaatio.setOid(OrganisaatioOidTestUtil.createOid());
        }
        else {
            organisaatio.setOid(oid);
        }

        organisaatio.setParentOid(parentOid);

        if (organisaatioTyyppi != null) {
            organisaatio.getTyypit().add(organisaatioTyyppi);
        }

        organisaatio.setAlkuPvm(DEFAULT_VOIMASSAOLO_ALKU);
        organisaatio.setKotipaikkaUri(DEFAULT_KOTIPAIKKA);
        organisaatio.setMaaUri(DEFAULT_MAA);
        organisaatio.setKieletUris(DEFAULT_KIELET);

        organisaatio.getYhteystiedot().add(DEFAULT_POSTIOSOITE);
        organisaatio.getYhteystiedot().add(DEFAULT_KAYNTIOSOITE);
        organisaatio.getYhteystiedot().add(DEFAULT_PUHELIN);
        organisaatio.getYhteystiedot().add(DEFAULT_WWW);
        organisaatio.getYhteystiedot().add(DEFAULT_EMAIL);

        return organisaatio;
    }
}
