package fi.vm.sade.organisaatio.dto;

import java.util.Date;
import java.util.List;

public class OrganisaatioPerustietoRivi {

    private String oid;
    private Date alkuPvm;
    private Date lakkautusPvm;
    private String parentOidPath;
    private String parentOid;
    private String ytunnus;
    private String virastotunnus;
    private String oppilaitosKoodi;
    private String oppilaitostyyppi;
    private String toimipistekoodi;
    private String nimiKieli;
    private String nimiArvo;
    private String tyyppi;
    private String kieli;
    private String kotipaikka;

    public OrganisaatioPerustietoRivi(String oid, Date alkuPvm, Date lakkautusPvm, String parentOidPath,
                                      String parentOid, String ytunnus, String virastotunnus, String oppilaitosKoodi,
                                      String oppilaitostyyppi, String toimipistekoodi, String nimiKieli,
                                      String nimiArvo, String tyyppi, String kieli, String kotipaikka) {
        this.oid = oid;
        this.alkuPvm = alkuPvm;
        this.lakkautusPvm = lakkautusPvm;
        this.parentOidPath = parentOidPath;
        this.parentOid = parentOid;
        this.ytunnus = ytunnus;
        this.virastotunnus = virastotunnus;
        this.oppilaitosKoodi = oppilaitosKoodi;
        this.oppilaitostyyppi = oppilaitostyyppi;
        this.toimipistekoodi = toimipistekoodi;
        this.nimiKieli = nimiKieli;
        this.nimiArvo = nimiArvo;
        this.tyyppi = tyyppi;
        this.kieli = kieli;
        this.kotipaikka = kotipaikka;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public Date getAlkuPvm() {
        return alkuPvm;
    }

    public void setAlkuPvm(Date alkuPvm) {
        this.alkuPvm = alkuPvm;
    }

    public Date getLakkautusPvm() {
        return lakkautusPvm;
    }

    public void setLakkautusPvm(Date lakkautusPvm) {
        this.lakkautusPvm = lakkautusPvm;
    }

    public String getParentOidPath() {
        return parentOidPath;
    }

    public void setParentOidPath(String parentOidPath) {
        this.parentOidPath = parentOidPath;
    }

    public String getParentOid() {
        return parentOid;
    }

    public void setParentOid(String parentOid) {
        this.parentOid = parentOid;
    }

    public String getYtunnus() {
        return ytunnus;
    }

    public void setYtunnus(String ytunnus) {
        this.ytunnus = ytunnus;
    }

    public String getVirastotunnus() {
        return virastotunnus;
    }

    public void setVirastotunnus(String virastotunnus) {
        this.virastotunnus = virastotunnus;
    }

    public String getOppilaitosKoodi() {
        return oppilaitosKoodi;
    }

    public void setOppilaitosKoodi(String oppilaitosKoodi) {
        this.oppilaitosKoodi = oppilaitosKoodi;
    }

    public String getOppilaitostyyppi() {
        return oppilaitostyyppi;
    }

    public void setOppilaitostyyppi(String oppilaitostyyppi) {
        this.oppilaitostyyppi = oppilaitostyyppi;
    }

    public String getToimipistekoodi() {
        return toimipistekoodi;
    }

    public void setToimipistekoodi(String toimipistekoodi) {
        this.toimipistekoodi = toimipistekoodi;
    }

    public String getNimiKieli() {
        return nimiKieli;
    }

    public void setNimiKieli(String nimiKieli) {
        this.nimiKieli = nimiKieli;
    }

    public String getNimiArvo() {
        return nimiArvo;
    }

    public void setNimiArvo(String nimiArvo) {
        this.nimiArvo = nimiArvo;
    }

    public String getTyyppi() {
        return tyyppi;
    }

    public void setTyyppi(String tyyppi) {
        this.tyyppi = tyyppi;
    }

    public String getKieli() {
        return kieli;
    }

    public void setKieli(String kieli) {
        this.kieli = kieli;
    }

    public String getKotipaikka() {
        return kotipaikka;
    }

    public void setKotipaikka(String kotipaikka) {
        this.kotipaikka = kotipaikka;
    }

}
