/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
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
package fi.vm.sade.organisaatio.service.converter;

import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioNimiModelMapper;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioMetaDataRDTO;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioNimiRDTO;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import org.apache.solr.common.util.Base64;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 *
 * @author mlyly, simok
 */
public class OrganisaatioToOrganisaatioRDTOConverter extends AbstractFromDomainConverter<Organisaatio, OrganisaatioRDTO> {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioToOrganisaatioRDTOConverter.class);

    private final OrganisaatioNimiModelMapper organisaatioNimiModelMapper;
    private final Type organisaatioNimiRDTOListType;

    public OrganisaatioToOrganisaatioRDTOConverter() {
        this.organisaatioNimiRDTOListType = new TypeToken<List<OrganisaatioNimiRDTO>>() {}.getType();
        this.organisaatioNimiModelMapper = new OrganisaatioNimiModelMapper();
    }


    @Override
    public OrganisaatioRDTO convert(Organisaatio s) {
        long qstarted = System.currentTimeMillis();

        OrganisaatioRDTO t = new OrganisaatioRDTO();

        t.setOid(s.getOid());
        t.setVersion(s.getVersion() != null ? s.getVersion().intValue() : 0);

        t.setAlkuPvm(s.getAlkuPvm());
        t.setDomainNimi(s.getDomainNimi());

        t.setKayntiosoite(convertOsoiteToMap(s.getKayntiosoite()));

        t.setKieletUris(convertListToList(s.getKielet()));
        t.setKotipaikkaUri(s.getKotipaikka());
        t.setKuvaus2(convertMKTToMap(s.getKuvaus2()));
        t.setLakkautusPvm(s.getLakkautusPvm());
        t.setMaaUri(s.getMaa());
        t.setMetadata(convertMetadata(s.getMetadata()));
        t.setNimi(convertMKTToMap(s.getNimi()));

        t.setNimet((List<OrganisaatioNimiRDTO>) organisaatioNimiModelMapper.map(s.getNimet(), organisaatioNimiRDTOListType));

        t.setStatus(s.getStatus().name());

        t.setOppilaitosKoodi(s.getOppilaitosKoodi());
        t.setOppilaitosTyyppiUri(s.getOppilaitosTyyppi());
        t.setParentOid(s.getParent() != null ? s.getParent().getOid() : null);
        t.setParentOidPath(s.getParentOidPath());


        t.setPostiosoite(convertOsoiteToMap(s.getPostiosoite()));

        t.setOpetuspisteenJarjNro(s.getOpetuspisteenJarjNro());
        t.setToimipistekoodi(s.getToimipisteKoodi());
        t.setTyypit(convertListToList(s.getTyypit()));
        t.setVuosiluokat(convertListToList(s.getVuosiluokat()));
        // tuetaan vanhaa formaattia ryhmätyypeille ja käyttöryhmille
        t.setRyhmatyypit(convertListToList(s.getRyhmatyypitV1()));
        t.setKayttoryhmat(convertListToList(s.getKayttoryhmatV1()));
        t.setYhteishaunKoulukoodi(s.getYhteishaunKoulukoodi());
        t.setYritysmuoto(s.getYritysmuoto());
        t.setYTJKieli(s.getYtjKieli());
        t.setYTJPaivitysPvm(s.getYtjPaivitysPvm());
        t.setYTunnus(s.getYtunnus());
        t.setVirastoTunnus(s.getVirastoTunnus());

        // Get dynamic Yhteysieto / Yhteystietotyppie / Elementti data
        List<Map<String, String>> yhteystietoArvos = new ArrayList<>();
        t.setYhteystietoArvos(yhteystietoArvos);

        for (Yhteystieto y : s.getYhteystiedot()) {
            t.addYhteystieto(convertYhteystietoGeneric(y));
        }

        for (YhteystietoArvo yhteystietoArvo : s.getYhteystietoArvos()) {
            YhteystietoElementti yElementti;
            YhteystietojenTyyppi yTyyppi;

            Map<String, String> val = new HashMap<>();
            yhteystietoArvos.add(val);

            val.put("YhteystietoArvo.arvoText", yhteystietoArvo.getArvoText());
            val.put("YhteystietoArvo.kieli", yhteystietoArvo.getKieli());

            yElementti = yhteystietoArvo.getKentta();

            if (yElementti != null) {
                val.put("YhteystietoElementti.nimi", yElementti.getNimi());
                val.put("YhteystietoElementti.nimiSv", yElementti.getNimiSv());
                val.put("YhteystietoElementti.oid", yElementti.getOid());
                val.put("YhteystietoElementti.tyyppi", yElementti.getTyyppi());
                val.put("YhteystietoElementti.kaytossa", "" + yElementti.isKaytossa());
                val.put("YhteystietoElementti.pakollinen", "" + yElementti.isPakollinen());

                yTyyppi = yElementti.getYhteystietojenTyyppi();

                if (yTyyppi != null) {
                    Map<String, String> nimiMap = convertMKTToMap(yTyyppi.getNimi());
                    for (String kieli : nimiMap.keySet()) {
                        val.put("YhteystietojenTyyppi.nimi." + kieli, nimiMap.get(kieli));
                    }

                    val.put("YhteystietojenTyyppi.oid", yTyyppi.getOid());
                }
            }
        }

        LOG.debug("convert: {} --> " + t.getClass().getSimpleName() + " in {} ms", s, System.currentTimeMillis() - qstarted);

        return t;
    }

    private Map<String, String> convertMKTToMap(MonikielinenTeksti nimi) {
        Map<String, String> result = new HashMap<>();

        if (nimi != null) {
            // Lisätään vastauksiin kaikki nimen kielet, joissa tekstiä
            for (Map.Entry<String, String> entry : nimi.getValues().entrySet()) {
                if (isNullOrEmpty(entry.getValue()) == false) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return result;
    }

    private List<String> convertListToList(Collection<String> s) {
        List<String> result = new ArrayList<>();
        for (String v : s) {
            result.add(v);
        }
        return result;
    }

    private String formatDate(Date dt) {
        if (dt != null) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            return df.format(dt);
        } else {
            return null;
        }
    }

    private Map<String, String> convertOsoiteToMap(Osoite s) {
        Map<String, String> t = new HashMap<>();

        if (s == null) {
            return null;
        }

        addToMapIfNotNULL(t, "coordinateType", s.getCoordinateType());
        addToMapIfNotNULL(t, "extraRivi", s.getExtraRivi());
        addToMapIfNotNULL(t, "maaUri", s.getMaa());
        addToMapIfNotNULL(t, "osavaltio", s.getOsavaltio());
        addToMapIfNotNULL(t, "osoite", s.getOsoite().replace("<br />", "\n"));
        addToMapIfNotNULL(t, "osoiteTyyppi", s.getOsoiteTyyppi());
        addToMapIfNotNULL(t, "postinumeroUri", s.getPostinumero());
        addToMapIfNotNULL(t, "postitoimipaikka", s.getPostitoimipaikka());
        addToMapIfNotNULL(t, "yhteystietoOid", s.getYhteystietoOid());
        addToMapIfNotNULL(t, "lap", s.getLat());
        addToMapIfNotNULL(t, "lng", s.getLng());

        addToMapIfNotNULL(t, "ytjPaivitysPvm", formatDate(s.getYtjPaivitysPvm()));

        return t;
    }

    private void addToMapIfNotNULL(Map map, String key, Object value) {
//        if (value != null) {
        map.put(key, value);
//        }
    }

    private OrganisaatioMetaDataRDTO convertMetadata(OrganisaatioMetaData s) {
        if (s == null) {
            return null;
        }

        OrganisaatioMetaDataRDTO t = new OrganisaatioMetaDataRDTO();

        t.setHakutoimistoEctsEmail(convertMKTToMap(s.getHakutoimistoEctsEmail()));
        t.setHakutoimistoEctsNimi(convertMKTToMap(s.getHakutoimistoEctsNimi()));
        t.setHakutoimistoEctsPuhelin(convertMKTToMap(s.getHakutoimistoEctsPuhelin()));
        t.setHakutoimistoEctsTehtavanimike(convertMKTToMap(s.getHakutoimistoEctsTehtavanimike()));
        t.setHakutoimistonNimi(convertMKTToMap(s.getHakutoimistoNimi()));
        t.setKoodi(s.getKoodi());

        // Otetaan kuva mukaan vain "pyydettäessä"
        if (s.isIncludeImage()) {
            t.setKuvaEncoded(encodeToUUENCODED(s.getKuva()));
        }
        t.setLuontiPvm(s.getLuontiPvm());
        t.setMuokkausPvm(s.getMuokkausPvm());
        t.setNimi(convertMKTToMap(s.getNimi()));

        // TODO t.set(s.getYhteystiedot());
        for (Yhteystieto yhteystieto : s.getYhteystiedot()) {
            t.getYhteystiedot().add(convertYhteystietoGeneric(yhteystieto));
        }


        for (NamedMonikielinenTeksti namedMonikielinenTeksti : s.getValues()) {
            // TODO how about namedMonikielinenTeksti.getNimi ???
            t.addByKey(namedMonikielinenTeksti.getKey(), convertMKTToMap(namedMonikielinenTeksti.getValue()));
        }

        return t;
    }


    /**
     * Converts any Yhteystieto to a MAP.
     *
     * @param s
     * @return
     */
    private Map<String, String> convertYhteystietoGeneric(Yhteystieto s) {
        Map<String, String> result = new HashMap<>();

        if (s != null) {
            result.put("id", "" + s.getId());
            result.put("yhteystietoOid", s.getYhteystietoOid());
            result.put("kieli", s.getKieli());

            if (s instanceof Email) {
                Email v = (Email) s;
                result.put("email", v.getEmail());
            } else if (s instanceof Www) {
                Www v = (Www) s;
                result.put("www", v.getWwwOsoite());
            } else if (s instanceof Puhelinnumero) {
                Puhelinnumero v = (Puhelinnumero) s;
                result.put("numero", v.getPuhelinnumero());
                result.put("tyyppi", v.getTyyppi());
            } else if (s instanceof Osoite) {
                Osoite v = (Osoite) s;
                result.putAll(convertOsoiteToMap(v));
            } else {
                result.put("ERROR", "UNKNOWN TYPE OF: " + s.getClass().getSimpleName());
            }
        }

        return result;
    }

    private String encodeToUUENCODED(BinaryData kuva) {
        if (kuva == null || kuva.getData() == null) {
            return null;
        }

        return Base64.byteArrayToBase64(kuva.getData(), 0, kuva.getData().length);
    }

    public String convertYhteystietoToPuhelinnumero(List<Yhteystieto> yhteystietos) {

        for (Yhteystieto yhteystieto : yhteystietos) {
            if (yhteystieto instanceof Puhelinnumero) {
                Puhelinnumero p = (Puhelinnumero) yhteystieto;
                if (Puhelinnumero.TYYPPI_PUHELIN.equals(p.getTyyppi())) {
                    return p.getPuhelinnumero();
                }
            }
        }
        return null;
    }

    public String convertYhteystietoToWwwOsoite(List<Yhteystieto> yhteystietos) {

        for (Yhteystieto yhteystieto : yhteystietos) {
            if (yhteystieto instanceof Www) {
                return ((Www) yhteystieto).getWwwOsoite();
            }
        }
        return null;
    }

    public String convertYhteystietoToEmailOsoite(List<Yhteystieto> yhteystietos) {

        for (Yhteystieto yhteystieto : yhteystietos) {
            if (yhteystieto instanceof Email) {
                return ((Email) yhteystieto).getEmail();
            }
        }
        return null;
    }

    private boolean isEmpty(String s) {
        return (s == null || s.trim().isEmpty());
    }

}
