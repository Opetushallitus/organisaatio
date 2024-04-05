package fi.vm.sade.organisaatio.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class KoodiType {
    protected String koodiUri;
    protected String resourceUri;
    protected KoodistoItemType koodisto;
    protected int versio;
    protected String koodiArvo;
    protected String paivitysPvm;
    protected String voimassaAlkuPvm;
    protected String voimassaLoppuPvm;
    protected TilaType tila;
    protected List<KoodiMetadataType> metadata = new ArrayList<>();
    protected long lockingVersion;

    @Data
    public static class KoodistoItemType {
        protected String koodistoUri;
        protected String organisaatioOid;
        protected List<Integer> koodistoVersio;
    }

    @Data
    public static class KoodiMetadataType {
        protected KieliType kieli;
        protected String nimi;
        protected String kuvaus;
        protected String lyhytNimi;
        protected String kayttoohje;
        protected String kasite;
        protected String sisaltaaMerkityksen;
        protected String eiSisallaMerkitysta;
        protected String huomioitavaKoodi;
        protected String sisaltaaKoodiston;
    }

    public static enum KieliType {
        FI,
        SV,
        EN;

        public String value() {
            return name();
        }

        public static KieliType fromValue(String v) {
            return valueOf(v);
        }

    }

    public static enum TilaType {
        PASSIIVINEN,
        LUONNOS,
        HYVAKSYTTY;

        public String value() {
            return name();
        }

        public static TilaType fromValue(String v) {
            return valueOf(v);
        }
    }
}
