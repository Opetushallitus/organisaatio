package fi.vm.sade.organisaatio.service.converter;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioNimiModelMapper;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioMetaDataRDTO;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.service.util.OrganisaatioNimiUtil;
import fi.vm.sade.organisaatio.service.util.OrganisaatioUtil;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class OrganisaatioRDTOToOrganisaatioConverter implements Converter<OrganisaatioRDTO, Organisaatio> {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioRDTOToOrganisaatioConverter.class);

    private final OrganisaatioNimiModelMapper organisaatioNimiModelMapper;

    public OrganisaatioRDTOToOrganisaatioConverter(OrganisaatioNimiModelMapper organisaatioNimiModelMapper) {
        this.organisaatioNimiModelMapper = organisaatioNimiModelMapper;
    }

    @Override
    public Organisaatio convert(OrganisaatioRDTO t) {
        //SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Set<Yhteystieto> yhteystietos = new HashSet<>();
        Organisaatio s = new Organisaatio();

        s.setOid(t.getOid());
        s.setVersion((long)t.getVersion());

        s.setAlkuPvm(t.getAlkuPvm());
        // t.setChildCount(s.getChildCount());
        s.setDomainNimi(t.getDomainNimi());

        s.setKielet(convertCollectionToSet(t.getKieletUris()));
        s.setKotipaikka(t.getKotipaikkaUri());
        s.setKuvaus2(convertMapToMonikielinenTeksti(t.getKuvaus2()));
        s.setLakkautusPvm(t.getLakkautusPvm());
        s.setMaa(t.getMaaUri());
        s.setMetadata(convertMetadata(t.getMetadata()));
        s.setNimi(convertMapToMonikielinenTeksti(t.getNimi()));

        // Define the target list type for mapping
        Type organisaatioNimiListType = new TypeToken<List<OrganisaatioNimi>>() {}.getType();

        // Map DTO to domain type
        s.setNimet(organisaatioNimiModelMapper.map(t.getNimet(), organisaatioNimiListType));

        // Asetetaan nimihakuun nimeksi nimihistorian current nimi, tai uusin nimi
        MonikielinenTeksti nimi = OrganisaatioNimiUtil.getNimi(s.getNimet());
        s.setNimihaku(OrganisaatioNimiUtil.createNimihaku(nimi));

        // t.set(s.getNimiLyhenne());
        s.setOpetuspisteenJarjNro(t.getOpetuspisteenJarjNro());
        s.setOppilaitosKoodi(t.getOppilaitosKoodi());
        s.setOppilaitosTyyppi(t.getOppilaitosTyyppiUri());
        // s.setParentOid(s.getParent() != null ? s.getParent().getOid() : null);
        // t.set(s.getParentIdPath());
        // t.setParentMetadata(s.getParentMetadata());
        s.setParentOids(OrganisaatioUtil.parentOids(s.getParentOidPath()));
        // t.set(s.getParentSuhteet());

        // t.set(s.getPuhelin());
        s.setToimipisteKoodi(t.getToimipistekoodi());
        s.setTyypit(OrganisaatioTyyppi.tyypitToKoodis(t.getTyypit()));
        // t.set(s.getTyypitAsString());
        s.setVuosiluokat(convertCollectionToSet(t.getVuosiluokat()));
        // tuetaan vanhaa formaattia ryhmätyypeille ja käyttöryhmille
        s.setRyhmatyypitV1(convertCollectionToSet(t.getRyhmatyypit()));
        s.setKayttoryhmatV1(convertCollectionToSet(t.getKayttoryhmat()));
        s.setYhteishaunKoulukoodi(t.getYhteishaunKoulukoodi());
        // t.set(s.getYhteystiedot());
        // t.set(s.getYhteystietoArvos());
        s.setYritysmuoto(t.getYritysmuoto());
        s.setYtjKieli(t.getYTJKieli());
        s.setYtjPaivitysPvm(t.getYTJPaivitysPvm());
        s.setYtunnus(t.getYTunnus());
        s.setVirastoTunnus(t.getVirastoTunnus());
        s.setTarkastusPvm(t.getTarkastusPvm());

        if (t.getYhteystietoArvos()!=null) {
            s.setYhteystietoArvos(convertYhteystietoArvos(t.getYhteystietoArvos()));
        }

        for (Map<String, String> m : t.getYhteystiedot()) {
            Yhteystieto y = convertYhteystietoGeneric(m);
            if (y != null) {
                yhteystietos.add(y);
            }
        }
        s.setYhteystiedot(yhteystietos);

        return s;
    }

    private Set<YhteystietoArvo> convertYhteystietoArvos(Set<Map<String, String>> arvoMaps) {
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

    private Osoite convertMapToOsoite(Map<String, String> s, String tyyppi) {
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

    private MonikielinenTeksti convertYATToMonikielinenTeksti(Map<String, String> m) {
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

    private MonikielinenTeksti convertMapToMonikielinenTeksti(Map<String, String> m) {
        MonikielinenTeksti mt = null;
        if (m != null) {
            mt = new MonikielinenTeksti();
            for (Map.Entry<String, String> e : m.entrySet()) {
                mt.addString(e.getKey(), e.getValue());
            }
        }
        return mt;
    }

    private Puhelinnumero convertPuhelinnumero(String numero, String tyyppi) {
        Puhelinnumero p = new Puhelinnumero();
        p.setPuhelinnumero(numero);
        p.setTyyppi(tyyppi);
        return p;
    }

    private Www convertWww(String wwwOsoite) {
        Www www = new Www();
        www.setWwwOsoite(wwwOsoite);
        return www;
    }

    private Email convertEmail(String emailOsoite) {
        Email email = new Email();
        email.setEmail(emailOsoite);
        return email;
    }

    private Set<String> convertCollectionToSet(Collection<String> a) {
        return new HashSet<>(a);
    }


    private OrganisaatioMetaData convertMetadata(OrganisaatioMetaDataRDTO t) {
        if (t == null) {
            return null;
        }

        OrganisaatioMetaData s = new OrganisaatioMetaData();

        s.setHakutoimistoEctsEmail(convertMapToMonikielinenTeksti(t.getHakutoimistoEctsEmail()));
        s.setHakutoimistoEctsNimi(convertMapToMonikielinenTeksti(t.getHakutoimistoEctsNimi()));
        s.setHakutoimistoEctsPuhelin(convertMapToMonikielinenTeksti(t.getHakutoimistoEctsPuhelin()));
        s.setHakutoimistoEctsTehtavanimike(convertMapToMonikielinenTeksti(t.getHakutoimistoEctsTehtavanimike()));
        s.setHakutoimistoNimi(convertMapToMonikielinenTeksti(t.getHakutoimistonNimi()));
        s.setKoodi(t.getKoodi());
        if (t.getLuontiPvm()!=null) {
            s.setLuontiPvm(t.getLuontiPvm());
        }
        if (t.getMuokkausPvm()!=null) {
            s.setMuokkausPvm(t.getMuokkausPvm());
        }
        s.setNimi(convertMapToMonikielinenTeksti(t.getNimi()));

        for (Map<String, String> yhteystieto : t.getYhteystiedot()) {
            Yhteystieto y = convertYhteystietoGeneric(yhteystieto);
            if (y != null) {
                s.getYhteystiedot().add(y);
            }
        }

        if (t.getData() != null) {
            Set<NamedMonikielinenTeksti> nmtSet = new HashSet<NamedMonikielinenTeksti>();
            for (Map.Entry<String, Map<String, String>> e : t.getData().entrySet()) {
                NamedMonikielinenTeksti nmt = new NamedMonikielinenTeksti();
                nmt.setKey(e.getKey());
                nmt.setValue(convertMapToMonikielinenTeksti(e.getValue()));
                nmtSet.add(nmt);
            }
            s.setValues(nmtSet);
        }

        return s;
    }

    private Yhteystieto convertYhteystietoGeneric(Map<String, String> s) {
        if (s != null) {
            try {
                if (s.containsKey("kieli") == false) {
                    LOG.warn("missing kieli from yhteystieto");
                }
                if (s.get("email") != null) {
                    Email v = convertEmail(s.get("email"));
                    if (s.containsKey("yhteystietoOid")) {
                        v.setId(s.get("id") != null ? Long.parseLong(s.get("id")) : null);
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
}
