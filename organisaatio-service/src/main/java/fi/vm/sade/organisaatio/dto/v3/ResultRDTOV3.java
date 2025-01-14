package fi.vm.sade.organisaatio.dto.v3;

import java.io.Serializable;

/**
 * Result wrapper for organization CU operations.
 */
public class ResultRDTOV3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum ResultStatus {
        OK,
        INFO,
        WARNING,
        VALIDATION,
        ERROR
    };

    private String info;

    private OrganisaatioRDTOV3 organisaatio;

    public ResultRDTOV3(OrganisaatioRDTOV3 organisaatio) {
        this.organisaatio = organisaatio;
    }

    public ResultRDTOV3(OrganisaatioRDTOV3 organisaatio, String info) {
        this.organisaatio = organisaatio;
        this.info = info;
    }

    public ResultStatus getStatus() {
        return getInfo() == null ? ResultRDTOV3.ResultStatus.OK : ResultRDTOV3.ResultStatus.WARNING;
    }

    public OrganisaatioRDTOV3 getOrganisaatio() {
        return organisaatio;
    }

    public void setOrganisaatio(OrganisaatioRDTOV3 organisaatio) {
        this.organisaatio = organisaatio;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

}
