
package fi.vm.sade.organisaatio.api.model.types;

import java.io.Serializable;

public class WwwDTO extends YhteystietoDTO implements Serializable  {
    private final static long serialVersionUID = 100L;
    protected String wwwOsoite;

    /**
     * Gets the value of the wwwOsoite property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWwwOsoite() {
        return wwwOsoite;
    }

    /**
     * Sets the value of the wwwOsoite property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWwwOsoite(String value) {
        this.wwwOsoite = value;
    }
}
