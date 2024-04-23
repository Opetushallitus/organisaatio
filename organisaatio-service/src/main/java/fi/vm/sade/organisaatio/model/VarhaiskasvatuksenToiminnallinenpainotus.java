package fi.vm.sade.organisaatio.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "varhaiskasvatuksen_toiminnallinenpainotus")
public class VarhaiskasvatuksenToiminnallinenpainotus extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "varhaiskasvatuksen_toimipaikka_tiedot_id")
    private VarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot;

    private String toiminnallinenpainotus;

    @Temporal(TemporalType.TIMESTAMP)
    private Date alkupvm;

    @Temporal(TemporalType.TIMESTAMP)
    private Date loppupvm;

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

    public VarhaiskasvatuksenToimipaikkaTiedot getVarhaiskasvatuksenToimipaikkaTiedot() {
        return varhaiskasvatuksenToimipaikkaTiedot;
    }

    public void setVarhaiskasvatuksenToimipaikkaTiedot(VarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot) {
        this.varhaiskasvatuksenToimipaikkaTiedot = varhaiskasvatuksenToimipaikkaTiedot;
    }

    public String getToiminnallinenpainotus() {
        return toiminnallinenpainotus;
    }

    public void setToiminnallinenpainotus(String toiminnallinenpainotus) {
        this.toiminnallinenpainotus = toiminnallinenpainotus;
    }
}
