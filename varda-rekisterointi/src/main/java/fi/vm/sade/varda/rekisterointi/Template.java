package fi.vm.sade.varda.rekisterointi;

public enum Template {

    /**
     * Sähköposti rekisteröinnin tehneelle henkilölle.
     */
    REKISTEROITYMINEN_KAYTTAJA("email/rekisteroityminen-kayttaja.html"),
    /**
     * Sähköposti rekisteröinnin yhteydessä ilmoitetulle Varda-pääkäyttäjälle.
     */
    REKISTEROITYMINEN_PAAKAYTTAJA("email/rekisteroityminen-paakayttaja.html"),
    /**
     * Sähköposti kuntaan.
     */
    REKISTEROITYMINEN_KUNTA("email/rekisteroityminen-kunta.html"),
    /**
     * Sähköposti kunnan hylkäämisen jälkeen kaikille rekisteröinnin yhteydessä ilmoitetuille henkilöille.
     */
    REKISTEROITYMINEN_HYLATTY("email/rekisteroityminen-hylatty.html"),
    /**
     * Sähköposti kunnan hyväksymisen jälkeen kaikille rekisteröinnin yhteydessä ilmoitetuille henkilöille.
     */
    REKISTEROITYMINEN_HYVAKSYTTY("email/rekisteroityminen-hyvaksytty.html"),
    ;

    private final String path;

    Template(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
