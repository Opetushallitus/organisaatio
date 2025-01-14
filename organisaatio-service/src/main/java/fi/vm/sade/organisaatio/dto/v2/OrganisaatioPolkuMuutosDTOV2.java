package fi.vm.sade.organisaatio.dto.v2;

import java.util.Date;

public class OrganisaatioPolkuMuutosDTOV2 {

    private String oid;
    private String parentOid;
    private String suhdeTyyppi;
    private Date alkuPvm;
    private Date loppuPvm;

    public Date getAlkuPvm() {
        return alkuPvm;
    }

    public Date getLoppuPvm() {
        return loppuPvm;
    }

    public String getOid() {
        return oid;
    }

    public String getParentOid() {
        return parentOid;
    }

    public String getSuhdeTyyppi() {
        return suhdeTyyppi;
    }
}
