
package fi.vm.sade.organisaatio.api.model.types;

import java.io.Serializable;

public abstract class YhteystietoDTO implements Serializable {
    private final static long serialVersionUID = 100L;
    protected long version;
    protected String yhteystietoOid;

    /**
     * Gets the value of the version property.
     * 
     */
    public long getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     */
    public void setVersion(long value) {
        this.version = value;
    }

    /**
     * Gets the value of the yhteystietoOid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getYhteystietoOid() {
        return yhteystietoOid;
    }

    /**
     * Sets the value of the yhteystietoOid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setYhteystietoOid(String value) {
        this.yhteystietoOid = value;
    }
}
