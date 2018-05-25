package fi.vm.sade.organisaatio.resource.dto;

import java.time.LocalDate;
import javax.ws.rs.QueryParam;

public class RyhmaCriteriaDto {

    @QueryParam("nimi")
    private String nimi;
    @QueryParam("aktiivinen")
    private Boolean aktiivinen;
    @QueryParam("lakkautusPvm")
    private LocalDate lakkautusPvm;
    @QueryParam("tyyppi")
    private String tyyppi;
    @QueryParam("kayttoryhma")
    private String kayttoryhma;

    public String getNimi() {
        return nimi;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }

    public Boolean getAktiivinen() {
        return aktiivinen;
    }

    public void setAktiivinen(Boolean aktiivinen) {
        this.aktiivinen = aktiivinen;
    }

    public LocalDate getLakkautusPvm() {
        return lakkautusPvm;
    }

    public void setLakkautusPvm(LocalDate lakkautusPvm) {
        this.lakkautusPvm = lakkautusPvm;
    }

    public String getTyyppi() {
        return tyyppi;
    }

    public void setTyyppi(String tyyppi) {
        this.tyyppi = tyyppi;
    }

    public String getKayttoryhma() {
        return kayttoryhma;
    }

    public void setKayttoryhma(String kayttoryhma) {
        this.kayttoryhma = kayttoryhma;
    }

}
