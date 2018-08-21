
package fi.vm.sade.organisaatio.api.model.types;

import java.io.Serializable;

public class SoMeLinkkiTyyppi implements Serializable {
    private final static long serialVersionUID = 100L;
    
    protected SoMeLinkkiTyyppiTyyppi tyyppi;
    protected String sisalto;

    /**
     * Gets the koodiValue of the tyyppi property.
     * 
     * @return
     *     possible object is
     *     {@link SoMeLinkkiTyyppiTyyppi }
     *     
     */
    public SoMeLinkkiTyyppiTyyppi getTyyppi() {
        return tyyppi;
    }

    /**
     * Sets the koodiValue of the tyyppi property.
     * 
     * @param value
     *     allowed object is
     *     {@link SoMeLinkkiTyyppiTyyppi }
     *     
     */
    public void setTyyppi(SoMeLinkkiTyyppiTyyppi value) {
        this.tyyppi = value;
    }

    /**
     * Gets the koodiValue of the sisalto property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSisalto() {
        return sisalto;
    }

    /**
     * Sets the koodiValue of the sisalto property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSisalto(String value) {
        this.sisalto = value;
    }
}
