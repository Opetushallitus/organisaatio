package fi.vm.sade.organisaatio.service.converter.v4;

import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioNimiModelMapper;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.service.converter.AbstractToDomainConverter;
import fi.vm.sade.organisaatio.service.converter.util.MetadataConverterUtils;
import fi.vm.sade.organisaatio.service.converter.util.MonikielinenTekstiConverterUtils;
import fi.vm.sade.organisaatio.service.converter.util.YhteystietoConverterUtils;
import fi.vm.sade.organisaatio.service.util.OrganisaatioNimiUtil;
import org.modelmapper.TypeToken;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class OrganisaatioRDTOV4ToOrganisaatioConverter extends AbstractToDomainConverter<OrganisaatioRDTOV4, Organisaatio> {

    @Override
    public Organisaatio convert(OrganisaatioRDTOV4 t) {
        List<Yhteystieto> yhteystietos = new ArrayList<>();
        Organisaatio s = new Organisaatio();

        s.setOid(t.getOid());
        s.setVersion((long)t.getVersion());

        s.setAlkuPvm(t.getAlkuPvm());
        s.setDomainNimi(t.getDomainNimi());

        s.setKielet(convertListToList(t.getKieletUris()));
        s.setKotipaikka(t.getKotipaikkaUri());
        s.setKuvaus2(MonikielinenTekstiConverterUtils.convertMapToMonikielinenTeksti(t.getKuvaus2()));
        s.setLakkautusPvm(t.getLakkautusPvm());
        s.setMaa(t.getMaaUri());
        s.setMetadata(MetadataConverterUtils.convertMetadata(t.getMetadata()));
        s.setNimi(MonikielinenTekstiConverterUtils.convertMapToMonikielinenTeksti(t.getNimi()));

        OrganisaatioNimiModelMapper organisaatioNimiModelMapper = new OrganisaatioNimiModelMapper();

        // Define the target list type for mapping
        Type organisaatioNimiListType = new TypeToken<List<OrganisaatioNimi>>() {}.getType();

        // Map DTO to domain type
        s.setNimet(organisaatioNimiModelMapper.map(t.getNimet(), organisaatioNimiListType));

        // Asetetaan nimihakuun nimeksi nimihistorian current nimi, tai uusin nimi
        MonikielinenTeksti nimi = OrganisaatioNimiUtil.getNimi(s.getNimet());
        if (nimi != null) {
            s.setNimihaku(convertNimiMapToNimihaku(nimi.getValues()));
        }

        s.setOpetuspisteenJarjNro(t.getOpetuspisteenJarjNro());
        s.setOppilaitosKoodi(t.getOppilaitosKoodi());
        s.setOppilaitosTyyppi(t.getOppilaitosTyyppiUri());
        s.setParentOidPath(s.getParentOidPath());

        s.setToimipisteKoodi(t.getToimipistekoodi());
        s.setTyypit(t.getTyypit());
        s.setVuosiluokat(convertListToList(t.getVuosiluokat()));
        s.setOrganisaatioLisatietotyypit(t.getLisatiedot().stream()
                .map(lisatietoNimi -> {
                    OrganisaatioLisatietotyyppi organisaatioLisatietotyyppi = new OrganisaatioLisatietotyyppi();
                    Lisatietotyyppi lisatietotyyppi = new Lisatietotyyppi();
                    lisatietotyyppi.setNimi(lisatietoNimi);
                    organisaatioLisatietotyyppi.setLisatietotyyppi(lisatietotyyppi);
                    organisaatioLisatietotyyppi.setOrganisaatio(s);
                    return organisaatioLisatietotyyppi;
                })
                .collect(Collectors.toSet()));
        s.setRyhmatyypit(convertSetToSet(t.getRyhmatyypit()));
        s.setKayttoryhmat(convertSetToSet(t.getKayttoryhmat()));
        s.setYhteishaunKoulukoodi(t.getYhteishaunKoulukoodi());
        s.setYritysmuoto(t.getYritysmuoto());
        s.setYtjKieli(t.getYTJKieli());
        s.setYtjPaivitysPvm(t.getYTJPaivitysPvm());
        s.setYtunnus(t.getYTunnus());
        s.setVirastoTunnus(t.getVirastoTunnus());

        if (t.getYhteystietoArvos()!=null) {
            s.setYhteystietoArvos(YhteystietoConverterUtils.convertYhteystietoArvos(t.getYhteystietoArvos()));
        }

        for (Map<String, String> m : t.getYhteystiedot()) {
            Yhteystieto y = YhteystietoConverterUtils.convertYhteystietoGeneric(m);
            if (y != null) {
                yhteystietos.add(y);
            }
        }
        s.setYhteystiedot(yhteystietos);

        return s;
    }

    private List<String> convertListToList(List<String> s) {
        return new ArrayList<>(s);
    }

    private Set<String> convertSetToSet(Set<String> s) {
        return new HashSet<>(s);
    }

    private String convertNimiMapToNimihaku(Map<String, String> nimiMap) {
        StringBuilder sb = new StringBuilder();
        for (String nimi : nimiMap.values()) {
                sb.append(",");
                sb.append(nimi);
        }
        return sb.toString();
    }
}
