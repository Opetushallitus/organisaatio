package fi.vm.sade.organisaatio.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "varhaiskasvatuksen_toimintamuoto")
public class VarhaiskasvatuksenToimintamuoto extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "varhaiskasvatuksen_toimipaikka_tiedot_id")
    private VarhaiskasvatuksenToimipaikkaTiedot varhaiskasvatuksenToimipaikkaTiedot;

    private String toimintamuoto;

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

    public String getToimintamuoto() {
        return toimintamuoto;
    }

    public void setToimintamuoto(String toimintamuoto) {
        this.toimintamuoto = toimintamuoto;
    }
}
