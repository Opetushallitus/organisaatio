package fi.vm.sade.tarjonta.service.types;

public enum KoulutusasteTyyppi {

    AMMATILLINEN_PERUSKOULUTUS("AmmatillinenPeruskoulutus"),
    LUKIOKOULUTUS("Lukiokoulutus"),
    KORKEAKOULUTUS("Korkeakoulutus"),
    PERUSOPETUKSEN_LISAOPETUS("PerusopetuksenLisaopetus"),
    VALMENTAVA_JA_KUNTOUTTAVA_OPETUS("ValmentavaJaKuntouttavaOpetus"),
    AMM_OHJAAVA_JA_VALMISTAVA_KOULUTUS("AmmOhjaavaJaValmistavaKoulutus"),
    MAAHANM_AMM_VALMISTAVA_KOULUTUS("MaahanmAmmValmistavaKoulutus"),
    MAAHANM_LUKIO_VALMISTAVA_KOULUTUS("MaahanmLukioValmistavaKoulutus"),
    VAPAAN_SIVISTYSTYON_KOULUTUS("VapaanSivistystyonKoulutus"),
    PERUSOPETUS("Perusopetus"),
    PERUSOPETUS_ULKOMAINEN("PerusopetusUlkomainen"),
    AMMATILLINEN_PERUSTUTKINTO_NAYTTOTUTKINTONA("AmmatillinenPerustutkintoNayttotutkintona"),
    ERIKOISAMMATTITUTKINTO("Erikoisammattitutkinto"),
    AMMATTITUTKINTO("Ammattitutkinto"),
    TUNTEMATON("Tuntematon");

    private final String value;

    KoulutusasteTyyppi(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static KoulutusasteTyyppi fromValue(String v) {
        for (KoulutusasteTyyppi c : KoulutusasteTyyppi.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
