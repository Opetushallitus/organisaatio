package fi.vm.sade.tarjonta.shared.types;

import java.util.HashSet;
import java.util.Set;

public enum ToteutustyyppiEnum {

    AMMATILLINEN_PERUSTUTKINTO("koulutustyyppi_1", true),
    LUKIOKOULUTUS("koulutustyyppi_2", true),
    KORKEAKOULUTUS("koulutustyyppi_3", false),
    AMMATILLINEN_PERUSKOULUTUS_ERITYISOPETUKSENA("koulutustyyppi_4", true),
    VALMENTAVA_JA_KUNTOUTTAVA_OPETUS_JA_OHJAUS("koulutustyyppi_5", true),
    PERUSOPETUKSEN_LISAOPETUS("koulutustyyppi_6", true),
    KORKEAKOULUOPINTO("koulutustyyppi_3", false),  // opintokokonaisuus or opintojakso
    AMMATILLISEEN_PERUSKOULUTUKSEEN_OHJAAVA_JA_VALMISTAVA_KOULUTUS("koulutustyyppi_7", true),
    AMMATILLISEEN_PERUSKOULUTUKSEEN_VALMENTAVA("koulutustyyppi_18", false),
    AMMATILLISEEN_PERUSKOULUTUKSEEN_VALMENTAVA_ER("koulutustyyppi_19", false),
    MAAHANMUUTTAJIEN_AMMATILLISEEN_PERUSKOULUTUKSEEN_VALMISTAVA_KOULUTUS("koulutustyyppi_8", true),
    MAAHANMUUTTAJIEN_JA_VIERASKIELISTEN_LUKIOKOULUTUKSEEN_VALMISTAVA_KOULUTUS("koulutustyyppi_9", true),
    VAPAAN_SIVISTYSTYON_KOULUTUS("koulutustyyppi_10", true),
    AMMATTITUTKINTO("koulutustyyppi_11", false),
    ERIKOISAMMATTITUTKINTO("koulutustyyppi_12", false),
    AMMATILLINEN_PERUSTUTKINTO_NAYTTOTUTKINTONA("koulutustyyppi_13", false),
    LUKIOKOULUTUS_AIKUISTEN_OPPIMAARA("koulutustyyppi_14", false),
    EB_RP_ISH("koulutustyyppi_21", false),
    ESIOPETUS("koulutustyyppi_15", false),
    PERUSOPETUS("koulutustyyppi_16", false),
    AIKUISTEN_PERUSOPETUS("koulutustyyppi_17", false),
    PELASTUSALAN_KOULUTUS("koulutustyyppi_24", false),
    AMMATILLINEN_PERUSTUTKINTO_ALK_2018("koulutustyyppi_26", true),
    ERIKOISAMMATTITUTKINTO_VALMISTAVA(null, false),
    AMMATTITUTKINTO_VALMISTAVA(null, false),
    AMMATILLINEN_PERUSTUTKINTO_NAYTTOTUTKINTONA_VALMISTAVA(null, false);

    private final String koulutustyyppiUri;
    private final boolean toisenAsteenKoulutus;

    ToteutustyyppiEnum(String koulutustyyppiUri, boolean toisenAsteenKoulutus) {
        this.koulutustyyppiUri = koulutustyyppiUri;
        this.toisenAsteenKoulutus = toisenAsteenKoulutus;
    }

    public String uri() {
        return this.koulutustyyppiUri;
    }

    public boolean isToisenAsteenKoulutus() {
        return toisenAsteenKoulutus;
    }

    /**
     * Convert string name or value to enumeration.
     *
     * @param strValue
     * @return
     */
    public static ToteutustyyppiEnum fromString(String strValue) {
        for (ToteutustyyppiEnum e : ToteutustyyppiEnum.values()) {
            if (e.koulutustyyppiUri != null && (e.koulutustyyppiUri.equals(strValue) || e.name().equals(strValue))) {
                return e;
            }
        }
        throw new IllegalArgumentException("Toteutustyyppi enum not found by value : '" + strValue + "'");
    }

    public static ToteutustyyppiEnum convertToValmistava(ToteutustyyppiEnum toteutustyyppi) {
        if (toteutustyyppi != null) {
            for (ToteutustyyppiEnum e : ToteutustyyppiEnum.values()) {
                if (e.koulutustyyppiUri == null && e.name().contains(toteutustyyppi.name())) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Valmistava toteutustyyppi enum not found by : '" + toteutustyyppi + "'");
    }

    public static boolean isValmistavaToteutustyyppi(ToteutustyyppiEnum tyyppi) {
        return getValmistavatToteutustyypit().contains(tyyppi);
    }

    public static Set<ToteutustyyppiEnum> getValmistavatToteutustyypit() {
        Set<ToteutustyyppiEnum> valmistavatToteutustyypit = new HashSet<>();

        valmistavatToteutustyypit.add(ERIKOISAMMATTITUTKINTO_VALMISTAVA);
        valmistavatToteutustyypit.add(AMMATTITUTKINTO_VALMISTAVA);
        valmistavatToteutustyypit.add(AMMATILLINEN_PERUSTUTKINTO_NAYTTOTUTKINTONA_VALMISTAVA);

        return valmistavatToteutustyypit;
    }

}
