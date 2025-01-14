package fi.vm.sade.organisaatio.dto.v2;

import java.util.Map;
import java.util.Set;

public class OrganisaatioYhteystiedotDTOV2 {
    
    private String oid;
    
    private Map<String, String> nimi;
    
    private Set<String> tyypit;

    private Set<String> kielet;
    
    private String kotipaikka;
    
    private String oppilaitosTyyppi;
    
    // Organisaatiotunniste saadaan vaikka näin --> CONCAT(oppilaitoskoodi, ytunnus, toimipistekoodi) as organisaatio_nro,
    private String oppilaitosKoodi;
    private String ytunnus;
    private String toimipisteKoodi;

    private Set<OsoiteDTOV2> postiosoite;

    private Set<OsoiteDTOV2> kayntiosoite;

    private Map<String, String> puhelinnumero;

    private Map<String, String> wwwOsoite;

    private Map<String, String> emailOsoite;
    
    
    /**
     * @return the oid
     */
    public String getOid() {
        return oid;
    }

    /**
     * @param oid the oid to set
     */
    public void setOid(String oid) {
        this.oid = oid;
    }

    /**
     * @return the nimi
     */
    public Map<String, String> getNimi() {
        return nimi;
    }

    /**
     * @param nimi the nimi to set
     */
    public void setNimi(Map<String, String> nimi) {
        this.nimi = nimi;
    }

    /**
     * @return the tyypit
     */
    public Set<String> getTyypit() {
        return tyypit;
    }

    /**
     * @param tyypit the tyypit to set
     */
    public void setTyypit(Set<String> tyypit) {
        this.tyypit = tyypit;
    }

    /**
     * @return the kotipaikka
     */
    public String getKotipaikka() {
        return kotipaikka;
    }

    /**
     * @param kotipaikka the kotipaikka to set
     */
    public void setKotipaikka(String kotipaikka) {
        this.kotipaikka = kotipaikka;
    }

    /**
     * @return the toimipistekoodi
     */
    public String getToimipisteKoodi() {
        return toimipisteKoodi;
    }

    /**
     * @param toimipisteKoodi the toimipistekoodi to set
     */
    public void setToimipisteKoodi(String toimipisteKoodi) {
        this.toimipisteKoodi = toimipisteKoodi;
    }

    /**
     * @return the kielet
     */
    public Set<String> getKielet() {
        return kielet;
    }

    /**
     * @param kielet the kielet to set
     */
    public void setKielet(Set<String> kielet) {
        this.kielet = kielet;
    }

    /**
     * @return the ytunnus
     */
    public String getYtunnus() {
        return ytunnus;
    }

    /**
     * @param ytunnus the ytunnus to set
     */
    public void setYtunnus(String ytunnus) {
        this.ytunnus = ytunnus;
    }

    /**
     * @return the oppilaitosKoodi
     */
    public String getOppilaitosKoodi() {
        return oppilaitosKoodi;
    }

    /**
     * @param oppilaitosKoodi the oppilaitosKoodi to set
     */
    public void setOppilaitosKoodi(String oppilaitosKoodi) {
        this.oppilaitosKoodi = oppilaitosKoodi;
    }

    /**
     * @return the postiosoite
     */
    public Set<OsoiteDTOV2> getPostiosoite() {
        return postiosoite;
    }

    /**
     * @param postiosoite the postiosoite to set
     */
    public void setPostiosoite(Set<OsoiteDTOV2> postiosoite) {
        this.postiosoite = postiosoite;
    }

    /**
     * @return the kayntiosoite
     */
    public Set<OsoiteDTOV2> getKayntiosoite() {
        return kayntiosoite;
    }

    /**
     * @param kayntiosoite the kayntiosoite to set
     */
    public void setKayntiosoite(Set<OsoiteDTOV2> kayntiosoite) {
        this.kayntiosoite = kayntiosoite;
    }

    /**
     * @return the puhelinnumero
     */
    public Map<String, String> getPuhelinnumero() {
        return puhelinnumero;
    }

    /**
     * @param puhelinnumero the puhelinnumero to set
     */
    public void setPuhelinnumero(Map<String, String> puhelinnumero) {
        this.puhelinnumero = puhelinnumero;
    }

    /**
     * @return the wwwOsoite
     */
    public Map<String, String> getWwwOsoite() {
        return wwwOsoite;
    }

    /**
     * @param wwwOsoite the wwwOsoite to set
     */
    public void setWwwOsoite(Map<String, String> wwwOsoite) {
        this.wwwOsoite = wwwOsoite;
    }

    /**
     * @return the emailOsoite
     */
    public Map<String, String> getEmailOsoite() {
        return emailOsoite;
    }

    /**
     * @param emailOsoite the emailOsoite to set
     */
    public void setEmailOsoite(Map<String, String> emailOsoite) {
        this.emailOsoite = emailOsoite;
    }

    /**
     * @return the oppilaitosTyyppi
     */
    public String getOppilaitosTyyppi() {
        return oppilaitosTyyppi;
    }

    /**
     * @param oppilaitosTyyppi the oppilaitosTyyppi to set
     */
    public void setOppilaitosTyyppi(String oppilaitosTyyppi) {
        this.oppilaitosTyyppi = oppilaitosTyyppi;
    }
}
