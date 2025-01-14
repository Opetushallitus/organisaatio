package fi.vm.sade.organisaatio.dto.v4;


import java.io.Serializable;

/**
 * Result wrapper for organization CU operations.
 */
public class ResultRDTOV4 implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum ResultStatus {
        OK,
        INFO,
        WARNING,
        VALIDATION,
        ERROR
    }

    private String info;

    private OrganisaatioRDTOV4 organisaatio;

    public ResultRDTOV4() {

    }

    public ResultRDTOV4(OrganisaatioRDTOV4 organisaatio) {
        this.organisaatio = organisaatio;
    }

    public ResultRDTOV4(OrganisaatioRDTOV4 organisaatio, String info) {
        this.organisaatio = organisaatio;
        this.info = info;
    }

    public ResultStatus getStatus() {
        return getInfo() == null ? ResultRDTOV4.ResultStatus.OK : ResultRDTOV4.ResultStatus.WARNING;
    }

    public OrganisaatioRDTOV4 getOrganisaatio() {
        return organisaatio;
    }

    public void setOrganisaatio(OrganisaatioRDTOV4 organisaatio) {
        this.organisaatio = organisaatio;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

}
