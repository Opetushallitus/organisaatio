package fi.vm.sade.oid;

public enum NodeClassCode {

    TEKN_5,
    TEKN_6,
    TOIMIPAIKAT,
    ASIAKIRJAT,
    OHJELMISTOT,
    LAITTEET,
    PALVELUT,
    LASKUTUS,
    LOGISTIIKKA,
    SANOMALIIKENNE,
    REKISTERINPITAJA,
    NAYTETUNNISTE,
    TILAP_ASIAKAS,
    HENKILO,
    ROOLI;

    public String value() {
        return name();
    }

    public static NodeClassCode fromValue(String v) {
        return valueOf(v);
    }

}
