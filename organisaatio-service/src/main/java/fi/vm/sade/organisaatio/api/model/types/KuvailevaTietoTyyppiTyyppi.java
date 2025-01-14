
package fi.vm.sade.organisaatio.api.model.types;

public enum KuvailevaTietoTyyppiTyyppi {
    YLEISKUVAUS("Yleiskuvaus"),
    ESTEETOMYYS("Esteetomyys"),
    OPPIMISYMPARISTO("Oppimisymparisto"),
    VUOSIKELLO("Vuosikello"),
    VASTUUHENKILOT("Vastuuhenkilot"),
    VALINTAMENETTELY("Valintamenettely"),
    AIEMMIN_HANKITTU_OSAAMINEN("AiemminHankittuOsaaminen"),
    KIELIOPINNOT("Kieliopinnot"),
    TYOHARJOITTELU("Tyoharjoittelu"),
    OPISKELIJALIIKKUVUUS("Opiskelijaliikkuvuus"),
    KANSAINVALISET_KOULUTUSOHJELMAT("KansainvalisetKoulutusohjelmat"),
    KUSTANNUKSET("Kustannukset"),
    RAHOITUS("Rahoitus"),
    OPISKELIJARUOKAILU("Opiskelijaruokailu"),
    TERVEYDENHUOLTOPALVELUT("Terveydenhuoltopalvelut"),
    VAKUUTUKSET("Vakuutukset"),
    OPISKELIJALIIKUNTA("Opiskelijaliikunta"),
    VAPAA_AIKA("VapaaAika"),
    OPISKELIJA_JARJESTOT("OpiskelijaJarjestot"),
    TIETOA_ASUMISESTA("TietoaAsumisesta");
    
    private final String value;

    KuvailevaTietoTyyppiTyyppi(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static KuvailevaTietoTyyppiTyyppi fromValue(String v) {
        for (KuvailevaTietoTyyppiTyyppi c: KuvailevaTietoTyyppiTyyppi.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
