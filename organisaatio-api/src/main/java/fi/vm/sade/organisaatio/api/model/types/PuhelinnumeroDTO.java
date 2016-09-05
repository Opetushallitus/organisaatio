
package fi.vm.sade.organisaatio.api.model.types;

import java.io.Serializable;

public class PuhelinnumeroDTO extends YhteystietoDTO implements Serializable  {
    private final static long serialVersionUID = 100L;
    
    protected String puhelinnumero;
    protected PuhelinNumeroTyyppi tyyppi;

    /**
     * Gets the value of the puhelinnumero property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPuhelinnumero() {
        return puhelinnumero;
    }

    /**
     * Sets the value of the puhelinnumero property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPuhelinnumero(String value) {
        this.puhelinnumero = value;
    }

    /**
     * Gets the value of the tyyppi property.
     * 
     * @return
     *     possible object is
     *     {@link PuhelinNumeroTyyppi }
     *     
     */
    public PuhelinNumeroTyyppi getTyyppi() {
        return tyyppi;
    }

    /**
     * Sets the value of the tyyppi property.
     * 
     * @param value
     *     allowed object is
     *     {@link PuhelinNumeroTyyppi }
     *     
     */
    public void setTyyppi(PuhelinNumeroTyyppi value) {
        this.tyyppi = value;
    }

}
