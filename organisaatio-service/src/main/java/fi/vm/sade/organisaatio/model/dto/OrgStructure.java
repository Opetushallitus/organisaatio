package fi.vm.sade.organisaatio.model.dto;

import com.querydsl.core.annotations.QueryProjection;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 18.3.2013
 * Time: 16:51
 * To change this template use File | Settings | File Templates.
 */
public class OrgStructure {

    @QueryProjection
    public OrgStructure(String oid, String oidPath,
                        MonikielinenTeksti name,
                        Boolean poistettu, Date lakkautusPvm) {
        this.oid = oid;
        this.oidPath = oidPath;
        this.name = name;
        this.poistettu = poistettu;
        this.lakkautusPvm = lakkautusPvm;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public void setOidPath(String oidPath) {
        this.oidPath = oidPath;
    }

    public String getOid() {
        return oid;
    }

    public String getOidPath() {
        return oidPath;
    }

    public MonikielinenTeksti getName() {
        return name;
    }

    public void setName(MonikielinenTeksti name) {
        this.name = name;
    }

    public Boolean getPoistettu() {
        return poistettu;
    }

    public void setPoistettu(Boolean poistettu) {
        this.poistettu = poistettu;
    }

    public Date getLakkautusPvm() {
        return lakkautusPvm;
    }

    public void setLakkautusPvm(Date lakkautusPvm) {
        this.lakkautusPvm = lakkautusPvm;
    }

    public String oid;


    public String oidPath;

    public MonikielinenTeksti name;

    public Boolean poistettu;

    public Date lakkautusPvm;


}
