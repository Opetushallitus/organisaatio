package fi.vm.sade.varda.rekisterointi;

public enum Template {

    /**
     * Sähköposti rekisteröinnin tehneelle henkilölle.
     */
    REKISTEROITYMINEN_KAYTTAJA("rekisteroityminen-kayttaja.html"),
    /**
     * Sähköposti rekisteröinnin yhteydessä ilmoitetulle Varda-pääkäyttäjälle.
     */
    REKISTEROITYMINEN_PAAKAYTTAJA("rekisteroityminen-paakayttaja.html"),
    /**
     * Sähköposti kuntaan.
     */
    REKISTEROITYMINEN_KUNTA("rekisteroityminen-kunta.html"),
    /**
     * Sähköposti kunnan hylkäämisen jälkeen kaikille rekisteröinnin yhteydessä ilmoitetuille henkilöille.
     */
    REKISTEROITYMINEN_HYLATTY("rekisteroityminen-hylatty.html"),
    /**
     * Sähköposti kunnan hyväksymisen jälkeen kaikille rekisteröinnin yhteydessä ilmoitetuille henkilöille.
     */
    REKISTEROITYMINEN_HYVAKSYTTY("rekisteroityminen-hyvaksytty.html"),

    AJASTETTUJEN_TASKIEN_VIRHERAPORTTI("taskien-virheraportti.html"),
    ;

    private final String path;

    Template(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
