package fi.vm.sade.organisaatio.dto.mapping;

import java.time.LocalDate;

public class RyhmaCriteriaDto {

    private String q;
    private Boolean aktiivinen;
    private LocalDate lakkautusPvm;
    private String ryhmatyyppi;
    private String kayttoryhma;
    private String parentOidPath;
    private Boolean poistettu;

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

    public String getParentOidPath() {
        return parentOidPath;
    }

    public void setParentOidPath(String parentOidPath) {
        this.parentOidPath = parentOidPath;
    }

    public Boolean getPoistettu() {
        return poistettu;
    }

    public void setPoistettu(Boolean poistettu) {
        this.poistettu = poistettu;
    }

}
