
package fi.vm.sade.organisaatio.api.model.types;

public class WwwDTO extends YhteystietoDTO {
    private final static long serialVersionUID = 100L;
    protected String wwwOsoite;

    /**
     * Gets the koodiValue of the wwwOsoite property.
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
     * Sets the koodiValue of the wwwOsoite property.
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
