package fi.vm.sade.organisaatio.dto.v4;

import fi.vm.sade.organisaatio.dto.v2.OrganisaatioGroupDTOV2;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.Set;

@Schema(description = "Organisaation ydintiedot",
          subTypes={OrganisaatioGroupDTOV2.class})
public class OrganisaatioCoreInfoDTOV4 {
    private String oid;
    private Map<String, String> nimi;
    private String status;
    private Set<String> _tyypit;

    /**
     * @return the oid
     */
    @Schema(description = "Organisaation oid", required = true)
    public String getOid() {
        return oid;
    }

    /**
     * @param oid the oid to set
     */
    public void setOid(String oid) {
        this.oid = oid;
    }

    /**
     * @return the status
     */
    @Schema(description = "Organisaation tila", required = true)
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Tyypit koodiarvona
     * @return the _tyypit
     */
    @Schema(description = "Organisaation tyypit", required = true)
    public Set<String> getTyypit() {
        return _tyypit;
    }

    /**
     * @param _tyypit the _tyypit to set
     */
    public void setTyypit(Set<String> _tyypit) {
        this._tyypit = _tyypit;
    }

    /**
     * @return the nimi
     */
    @Schema(description = "Organisaation nimi", required = true)
    public Map<String, String> getNimi() {
        return nimi;
    }

    /**
     * @param nimi the nimi to set
     */
    public void setNimi(Map<String, String> nimi) {
        this.nimi = nimi;
    }
}
