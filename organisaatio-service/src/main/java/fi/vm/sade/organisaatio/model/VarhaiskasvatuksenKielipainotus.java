package fi.vm.sade.organisaatio.model;

import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Embeddable
public class VarhaiskasvatuksenKielipainotus {
    private String kielipainotus;

    @Temporal(TemporalType.TIMESTAMP)
    private Date alkupvm;

    @Temporal(TemporalType.TIMESTAMP)
    private Date loppupvm;

    public String getKielipainotus() {
        return kielipainotus;
    }

    public void setKielipainotus(String kielipainotus) {
        this.kielipainotus = kielipainotus;
    }

    public Date getAlkupvm() {
        return alkupvm;
    }

    public void setAlkupvm(Date alkupvm) {
        this.alkupvm = alkupvm;
    }

    public Date getLoppupvm() {
        return loppupvm;
    }

    public void setLoppupvm(Date loppupvm) {
        this.loppupvm = loppupvm;
    }
}
