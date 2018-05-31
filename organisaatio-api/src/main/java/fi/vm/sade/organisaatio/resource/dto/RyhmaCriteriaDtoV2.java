package fi.vm.sade.organisaatio.resource.dto;

import io.swagger.annotations.ApiParam;
import java.time.LocalDate;
import javax.ws.rs.QueryParam;

public class RyhmaCriteriaDtoV2 {

    @QueryParam("q")
    private String q;
    @QueryParam("aktiivinen")
    private Boolean aktiivinen;
    @QueryParam("lakkautusPvm")
    private LocalDate lakkautusPvm;
    @QueryParam("ryhmatyyppi")
    @ApiParam(allowableValues = "organisaatio, hakukohde, perustetyoryhma, koulutus")
    private String ryhmatyyppi;
    @QueryParam("kayttoryhma")
    @ApiParam(allowableValues = "yleinen, hakukohde_rajaava, hakukohde_priorisoiva, hakukohde_liiteosoite, perusteiden_laadinta, kayttooikeus")
    private String kayttoryhma;

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
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

    public String getRyhmatyyppi() {
        return ryhmatyyppi;
    }

    public void setRyhmatyyppi(String ryhmatyyppi) {
        this.ryhmatyyppi = ryhmatyyppi;
    }

    public String getKayttoryhma() {
        return kayttoryhma;
    }

    public void setKayttoryhma(String kayttoryhma) {
        this.kayttoryhma = kayttoryhma;
    }

}
