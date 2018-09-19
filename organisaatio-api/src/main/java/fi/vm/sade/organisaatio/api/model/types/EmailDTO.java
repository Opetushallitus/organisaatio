
package fi.vm.sade.organisaatio.api.model.types;

import java.io.Serializable;

public class EmailDTO extends YhteystietoDTO implements Serializable  {
    private final static long serialVersionUID = 100L;
    protected String email;

    /**
     * Gets the value of the email property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the value of the email property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmail(String value) {
        this.email = value;
    }

}
