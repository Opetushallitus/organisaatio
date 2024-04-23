package fi.vm.sade.organisaatio.service.converter.util;

import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.service.converter.v4.OrganisaatioRDTOV4ToOrganisaatioConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;
import static fi.vm.sade.organisaatio.model.YhteystietoArvo.KRIISIVIESTINNAN_SAHKOPOSTIOSOITE_TYYPPI_OID;

public class YhteystietoConverterUtils {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioRDTOV4ToOrganisaatioConverter.class);

    public static void convertYhteystietosToListMap(Organisaatio s, Set<Map<String, String>> yhteystietoArvos) {
        for (YhteystietoArvo yhteystietoArvo : s.getYhteystietoArvos()) {
            YhteystietoElementti yElementti;
            YhteystietojenTyyppi yTyyppi;

            Map<String, String> val = new HashMap<>();

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
                    if (KRIISIVIESTINNAN_SAHKOPOSTIOSOITE_TYYPPI_OID.equals(yTyyppi.getOid())) break;
                    Map<String, String> nimiMap = convertMKTToMap(yTyyppi.getNimi());
                    for (String kieli : nimiMap.keySet()) {
                        val.put("YhteystietojenTyyppi.nimi." + kieli, nimiMap.get(kieli));
                    }

                    val.put("YhteystietojenTyyppi.oid", yTyyppi.getOid());
                }
            }
            yhteystietoArvos.add(val);
        }
    }

    public static Map<String, String> convertMKTToMap(MonikielinenTeksti nimi) {
        Map<String, String> result = new HashMap<>();

        if (nimi != null) {
            // Lisätään vastauksiin kaikki nimen kielet, joissa tekstiä
            for (Map.Entry<String, String> entry : nimi.getValues().entrySet()) {
                if (!isNullOrEmpty(entry.getValue())) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return result;
    }

    /**
     * Converts any Yhteystieto to a MAP.
     *
     * @param s Yhteystieto
     * @return Yhteystieto as map
     */
    public static Map<String, String> mapYhteystietoToGeneric(Yhteystieto s) {
        Map<String, String> result = new HashMap<>();

        if (s != null) {
            result.put("id", s.getId() != null ? String.valueOf(s.getId()) : null);
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


    public static Map<String, String> convertOsoiteToMap(Osoite s) {
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

    private static void addToMapIfNotNULL(Map map, String key, Object value) {
        map.put(key, value);
    }

    private static String formatDate(Date dt) {
        if (dt != null) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            return df.format(dt);
        } else {
            return null;
        }
    }


    public static Set<YhteystietoArvo> convertYhteystietoArvos(Set<Map<String, String>> arvoMaps) {
        Set<YhteystietoArvo> arvos = new HashSet<>(arvoMaps.size());
        for (Map<String, String> arvoMap : arvoMaps) {
            YhteystietoArvo arvo = new YhteystietoArvo();
            arvo.setKentta(new YhteystietoElementti());
            arvo.setKieli(arvoMap.get("YhteystietoArvo.kieli"));
            arvo.setArvoText(arvoMap.get("YhteystietoArvo.arvoText"));
            YhteystietoElementti ye = arvo.getKentta();
            ye.setNimi(arvoMap.get("YhteystietoElementti.nimi"));
            ye.setNimiSv(arvoMap.get("YhteystietoElementti.nimisv"));
            ye.setOid(arvoMap.get("YhteystietoElementti.oid"));
            ye.setTyyppi(arvoMap.get("YhteystietoElementti.tyyppi"));
            ye.setKaytossa(Boolean.parseBoolean(arvoMap.get("YhteystietoElementti.kaytossa")));
            ye.setPakollinen(Boolean.parseBoolean(arvoMap.get("YhteystietoElementti.pakollinen")));
            if (arvoMap.get("YhteystietojenTyyppi.oid") != null) {
                YhteystietojenTyyppi yt = new YhteystietojenTyyppi();
                yt.setOid(arvoMap.get("YhteystietojenTyyppi.oid"));
                yt.setNimi(convertYATToMonikielinenTeksti(arvoMap));
                ye.setYhteystietojenTyyppi(yt);
            }
            arvos.add(arvo);
        }
        return arvos;
    }

    private static MonikielinenTeksti convertYATToMonikielinenTeksti(Map<String, String> m) {
        MonikielinenTeksti mt = null;
        if (m != null) {
            mt = new MonikielinenTeksti();
            for (Map.Entry<String, String> e : m.entrySet()) {
                if (e.getKey().startsWith("YhteystietojenTyyppi.nimi.")) {
                    mt.addString(e.getKey().substring("YhteystietojenTyyppi.nimi.".length()), e.getValue());
                }
            }
        }
        return mt;
    }

    public static Yhteystieto convertYhteystietoGeneric(Map<String, String> s) {
        if (s != null) {
            try {
                if (!s.containsKey("kieli")) {
                    LOG.warn("missing kieli from yhteystieto");
                }
                if (s.get("email") != null) {
                    Email v = convertEmail(s.get("email"));
                    if (s.containsKey("yhteystietoOid")) {
                        v.setId(s.get("id") != null ? Long.parseLong(s.get("id")) : null);
                        v.setVersion(s.get("version") != null ? Long.parseLong(s.get("version")) : 0);
                        v.setYhteystietoOid(s.get("yhteystietoOid"));
                    }
                    if (s.containsKey("kieli")) {
                        v.setKieli(s.get("kieli"));
                    }
                    return v;
                } else if (s.get("www") != null) {
                    Www v = convertWww(s.get("www"));
                    if (s.containsKey("yhteystietoOid")) {
                        v.setId(s.get("id") != null ? Long.parseLong(s.get("id")) : null);
                        v.setVersion(s.get("version") != null ? Long.parseLong(s.get("version")) : 0);
                        v.setYhteystietoOid(s.get("yhteystietoOid"));
                    }
                    if (s.containsKey("kieli")) {
                        v.setKieli(s.get("kieli"));
                    }
                    return v;
                } else if (s.get("numero") != null) {
                    Puhelinnumero v = convertPuhelinnumero(s.get("numero"), s.get("tyyppi"));
                    if (s.containsKey("yhteystietoOid")) {
                        v.setId(s.get("id") != null ? Long.parseLong(s.get("id")) : null);
                        v.setVersion(s.get("version") != null ? Long.parseLong(s.get("version")) : 0);
                        v.setYhteystietoOid(s.get("yhteystietoOid"));
                    }
                    if (s.containsKey("kieli")) {
                        v.setKieli(s.get("kieli"));
                    }
                    return v;
                } else if (s.get("osoite") != null) {
                    Osoite v = convertMapToOsoite(s, null);
                    if (s.containsKey("yhteystietoOid")) {
                        Long id = s.get("id") != null ? Long.parseLong(s.remove("id")) : null;
                        v.setVersion(s.get("version") != null ? Long.parseLong(s.get("version")) : 0);
                        v.setId(id);
                    }
                    if (s.containsKey("kieli")) {
                        v.setKieli(s.get("kieli"));
                    }
                    return v;
                }
            }
            catch (NumberFormatException nfe) {
                LOG.error("failed parsing number", nfe);
            }
        }
        return null;
    }


    private static Osoite convertMapToOsoite(Map<String, String> s, String tyyppi) {
        if (s == null) {
            return null;
        }

        Osoite t = new Osoite();
        t.setOsoiteTyyppi(tyyppi);
        if (s.containsKey("coordinateType")) {
            t.setCoordinateType(s.get("coordinateType"));
        }
        if (s.containsKey("extraRivi")) {
            t.setExtraRivi(s.get("extraRivi"));
        }
        if (s.containsKey("maaUri")) {
            t.setMaa(s.get("maaUri"));
        }
        if (s.containsKey("osavaltio")) {
            t.setOsavaltio(s.get("osavaltio"));
        }
        if (s.containsKey("osoite")) {
            t.setOsoite(s.get("osoite").replace("\n", "<br />"));
        }
        if (s.containsKey("osoiteTyyppi")) {
            t.setOsoiteTyyppi(s.get("osoiteTyyppi"));
        }
        if (s.containsKey("postinumeroUri")) {
            t.setPostinumero(s.get("postinumeroUri"));
        }
        if (s.containsKey("postitoimipaikka")) {
            t.setPostitoimipaikka(s.get("postitoimipaikka"));
        }
        if (s.containsKey("yhteystietoOid")) {
            t.setYhteystietoOid(s.get("yhteystietoOid"));
        }
        if (s.get("lap") != null) {
            try {
                t.setLat(Double.parseDouble(s.get("lap")));
            }
            catch (NumberFormatException nfe) {
                // just don't set it then
            }
        }
        if (s.get("lng") != null) {
            try {
                t.setLng(Double.parseDouble(s.get("lng")));
            }
            catch (NumberFormatException nfe) {
                // just don't set it then
            }
        }
        if (s.get("ytjPaivitysPvm") != null) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            try {
                t.setYtjPaivitysPvm(df.parse(s.get("ytjPaivitysPvm")));
            }
            catch (ParseException pe) {
                // just don't set it then
            }
        }

        return t;
    }

    private static Puhelinnumero convertPuhelinnumero(String numero, String tyyppi) {
        Puhelinnumero p = new Puhelinnumero();
        p.setPuhelinnumero(numero);
        p.setTyyppi(tyyppi);
        return p;
    }

    private static Www convertWww(String wwwOsoite) {
        Www www = new Www();
        www.setWwwOsoite(wwwOsoite);
        return www;
    }

    private static Email convertEmail(String emailOsoite) {
        Email email = new Email();
        email.setEmail(emailOsoite);
        return email;
    }



}
