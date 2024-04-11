package fi.vm.sade.organisaatio.service.converter.v3;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.MappingProjection;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioNimiModelMapper;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioMetaDataRDTO;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioNimiRDTO;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;
import static fi.vm.sade.organisaatio.model.YhteystietoArvo.KRIISIVIESTINNAN_SAHKOPOSTIOSOITE_TYYPPI_OID;
import static fi.vm.sade.organisaatio.service.util.DateUtil.toTimestamp;

/**
 *  Organisaatio to OrganisaatioRDTOV3 projection. By doing the converstion on DAO layer avoid n+1 select when fetching multiple organisations.
 */
public class OrganisaatioToOrganisaatioRDTOV3ProjectionFactory extends MappingProjection<OrganisaatioRDTOV3> {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioToOrganisaatioRDTOV3ProjectionFactory.class);

    private final OrganisaatioNimiModelMapper organisaatioNimiModelMapper;
    private final Type organisaatioNimiRDTOListType;

    public OrganisaatioToOrganisaatioRDTOV3ProjectionFactory(Expression<Organisaatio> organisaatioExpression, OrganisaatioNimiModelMapper organisaatioNimiModelMapper) {
        super(OrganisaatioRDTOV3.class, organisaatioExpression);
        this.organisaatioNimiRDTOListType = new TypeToken<List<OrganisaatioNimiRDTO>>() {}.getType();
        this.organisaatioNimiModelMapper = organisaatioNimiModelMapper;
    }

    @Override
    protected OrganisaatioRDTOV3 map(Tuple row) {
        QOrganisaatio org = QOrganisaatio.organisaatio;
        Organisaatio s = row.get(org);
        long qstarted = System.currentTimeMillis();

        OrganisaatioRDTOV3 t = new OrganisaatioRDTOV3();

        t.setOid(s.getOid());
        t.setVersion(s.getVersion() != null ? s.getVersion().intValue() : 0);

        t.setAlkuPvm(s.getAlkuPvm());
        t.setDomainNimi(s.getDomainNimi());

        t.setKayntiosoite(convertOsoiteToMap(s.getKayntiosoite()));

        t.setKieletUris(convertCollectionToSet(s.getKielet()));
        t.setKotipaikkaUri(s.getKotipaikka());
        t.setKuvaus2(convertMKTToMap(s.getKuvaus2()));
        t.setLakkautusPvm(s.getLakkautusPvm());
        t.setMaaUri(s.getMaa());
        t.setMetadata(convertMetadata(s.getMetadata()));
        t.setNimi(convertMKTToMap(s.getNimi()));

        t.setNimet(organisaatioNimiModelMapper.map(s.getNimet(), organisaatioNimiRDTOListType));

        t.setStatus(s.getStatus().name());

        t.setOppilaitosKoodi(s.getOppilaitosKoodi());
        t.setOppilaitosTyyppiUri(s.getOppilaitosTyyppi());
        t.setParentOid(s.getParent() != null ? s.getParent().getOid() : null);
        t.setParentOidPath(s.getParentOidPath());


        t.setPostiosoite(convertOsoiteToMap(s.getPostiosoite()));

        t.setOpetuspisteenJarjNro(s.getOpetuspisteenJarjNro());
        t.setToimipistekoodi(s.getToimipisteKoodi());
        t.setTyypit(OrganisaatioTyyppi.tyypitFromKoodis(s.getTyypit()));
        t.setVuosiluokat(convertCollectionToSet(s.getVuosiluokat()));
        t.setRyhmatyypit(convertSetToSet(s.getRyhmatyypit()));
        t.setKayttoryhmat(convertSetToSet(s.getKayttoryhmat()));
        t.setYhteishaunKoulukoodi(s.getYhteishaunKoulukoodi());
        t.setYritysmuoto(s.getYritysmuoto());
        t.setYTJKieli(s.getYtjKieli());
        t.setYTJPaivitysPvm(s.getYtjPaivitysPvm());
        t.setYTunnus(s.getYtunnus());
        t.setVirastoTunnus(s.getVirastoTunnus());
        t.setTarkastusPvm(toTimestamp(s.getTarkastusPvm()));

        // Get dynamic Yhteysieto / Yhteystietotyppie / Elementti data
        Set<Map<String, String>> yhteystietoArvos = new HashSet<>();
        t.setYhteystietoArvos(yhteystietoArvos);

        for (Yhteystieto y : s.getYhteystiedot()) {
            t.addYhteystieto(convertYhteystietoGeneric(y));
        }

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

    private Set<String> convertCollectionToSet(Collection<String> s) {
        return new HashSet<>(s);
    }

    private Set<String> convertSetToSet(Collection<String> s) {
        return new HashSet<>(s);
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
//        if (koodiValue != null) {
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

        // Ei oteta ikinä kuvia mukaan

        t.setLuontiPvm(s.getLuontiPvm());
        t.setMuokkausPvm(s.getMuokkausPvm());
        t.setNimi(convertMKTToMap(s.getNimi()));

        for (Yhteystieto yhteystieto : s.getYhteystiedot()) {
            t.getYhteystiedot().add(convertYhteystietoGeneric(yhteystieto));
        }


        for (NamedMonikielinenTeksti namedMonikielinenTeksti : s.getValues()) {
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
            result.put("id", s.getId() != null ? String.valueOf(s.getId()) : null);
            result.put("version", s.getVersion() != null ? String.valueOf(s.getVersion()) : null);
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

}
