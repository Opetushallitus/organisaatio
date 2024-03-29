package fi.vm.sade.organisaatio.service.converter.util;

import fi.vm.sade.organisaatio.model.NamedMonikielinenTeksti;
import fi.vm.sade.organisaatio.model.OrganisaatioMetaData;
import fi.vm.sade.organisaatio.model.Yhteystieto;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioMetaDataRDTO;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MetadataConverterUtils {
    public static OrganisaatioMetaData convertMetadata(OrganisaatioMetaDataRDTO t) {
        if (t == null) {
            return null;
        }

        OrganisaatioMetaData s = new OrganisaatioMetaData();

        s.setHakutoimistoEctsEmail(MonikielinenTekstiConverterUtils.convertMapToMonikielinenTeksti(t.getHakutoimistoEctsEmail()));
        s.setHakutoimistoEctsNimi(MonikielinenTekstiConverterUtils.convertMapToMonikielinenTeksti(t.getHakutoimistoEctsNimi()));
        s.setHakutoimistoEctsPuhelin(MonikielinenTekstiConverterUtils.convertMapToMonikielinenTeksti(t.getHakutoimistoEctsPuhelin()));
        s.setHakutoimistoEctsTehtavanimike(MonikielinenTekstiConverterUtils.convertMapToMonikielinenTeksti(t.getHakutoimistoEctsTehtavanimike()));
        s.setHakutoimistoNimi(MonikielinenTekstiConverterUtils.convertMapToMonikielinenTeksti(t.getHakutoimistonNimi()));
        s.setKoodi(t.getKoodi());
        if (t.getLuontiPvm()!=null) {
            s.setLuontiPvm(t.getLuontiPvm());
        }
        if (t.getMuokkausPvm()!=null) {
            s.setMuokkausPvm(t.getMuokkausPvm());
        }
        s.setNimi(MonikielinenTekstiConverterUtils.convertMapToMonikielinenTeksti(t.getNimi()));

        for (Map<String, String> yhteystieto : t.getYhteystiedot()) {
            Yhteystieto y = YhteystietoConverterUtils.convertYhteystietoGeneric(yhteystieto);
            if (y != null) {
                s.getYhteystiedot().add(y);
            }
        }

        if (t.getData() != null) {
            Set<NamedMonikielinenTeksti> nmtSet = new HashSet<>();
            for (Map.Entry<String, Map<String, String>> e : t.getData().entrySet()) {
                NamedMonikielinenTeksti nmt = new NamedMonikielinenTeksti();
                nmt.setKey(e.getKey());
                nmt.setValue(MonikielinenTekstiConverterUtils.convertMapToMonikielinenTeksti(e.getValue()));
                nmtSet.add(nmt);
            }
            s.setValues(nmtSet);
        }

        return s;
    }


    public static OrganisaatioMetaDataRDTO convertMetadata(OrganisaatioMetaData s) {
        if (s == null) {
            return null;
        }

        OrganisaatioMetaDataRDTO t = new OrganisaatioMetaDataRDTO();

        t.setHakutoimistoEctsEmail(YhteystietoConverterUtils.convertMKTToMap(s.getHakutoimistoEctsEmail()));
        t.setHakutoimistoEctsNimi(YhteystietoConverterUtils.convertMKTToMap(s.getHakutoimistoEctsNimi()));
        t.setHakutoimistoEctsPuhelin(YhteystietoConverterUtils.convertMKTToMap(s.getHakutoimistoEctsPuhelin()));
        t.setHakutoimistoEctsTehtavanimike(YhteystietoConverterUtils.convertMKTToMap(s.getHakutoimistoEctsTehtavanimike()));
        t.setHakutoimistonNimi(YhteystietoConverterUtils.convertMKTToMap(s.getHakutoimistoNimi()));
        t.setKoodi(s.getKoodi());

        t.setLuontiPvm(s.getLuontiPvm());
        t.setMuokkausPvm(s.getMuokkausPvm());
        t.setNimi(YhteystietoConverterUtils.convertMKTToMap(s.getNimi()));

        for (Yhteystieto yhteystieto : s.getYhteystiedot()) {
            t.getYhteystiedot().add(YhteystietoConverterUtils.mapYhteystietoToGeneric(yhteystieto));
        }

        for (NamedMonikielinenTeksti namedMonikielinenTeksti : s.getValues()) {
            t.addByKey(namedMonikielinenTeksti.getKey(), YhteystietoConverterUtils.convertMKTToMap(namedMonikielinenTeksti.getValue()));
        }

        return t;
    }

}
