
package fi.vm.sade.organisaatio.api.model.types;

import java.io.Serializable;
import java.util.Date;


public class OsoiteDTO extends YhteystietoDTO implements Serializable  {
    private final static long serialVersionUID = 100L;
    protected String coordinateType;
    protected Double lat;
    protected Double lng;
    protected String maa;
    protected String osavaltio;
    protected String osoite;
    protected String extraRivi;
    protected String osoiteKayttotarkoitus;
    protected String osoiteMuuKaytto;
    protected OsoiteTyyppi osoiteTyyppi;
    protected String postinumero;
    protected String postitoimipaikka;
    protected Date ytjPaivitysPvm;

    /**
     * Gets the koodiValue of the coordinateType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCoordinateType() {
        return coordinateType;
    }

    /**
     * Sets the koodiValue of the coordinateType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCoordinateType(String value) {
        this.coordinateType = value;
    }

    /**
     * Gets the koodiValue of the lat property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getLat() {
        return lat;
    }

    /**
     * Sets the koodiValue of the lat property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setLat(Double value) {
        this.lat = value;
    }

    /**
     * Gets the koodiValue of the lng property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getLng() {
        return lng;
    }

    /**
     * Sets the koodiValue of the lng property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setLng(Double value) {
        this.lng = value;
    }

    /**
     * Gets the koodiValue of the maa property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaa() {
        return maa;
    }

    /**
     * Sets the koodiValue of the maa property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaa(String value) {
        this.maa = value;
    }

    /**
     * Gets the koodiValue of the osavaltio property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOsavaltio() {
        return osavaltio;
    }

    /**
     * Sets the koodiValue of the osavaltio property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOsavaltio(String value) {
        this.osavaltio = value;
    }

    /**
     * Gets the koodiValue of the osoite property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOsoite() {
        return osoite;
    }

    /**
     * Sets the koodiValue of the osoite property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOsoite(String value) {
        this.osoite = value;
    }

    /**
     * Gets the koodiValue of the extraRivi property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExtraRivi() {
        return extraRivi;
    }

    /**
     * Sets the koodiValue of the extraRivi property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExtraRivi(String value) {
        this.extraRivi = value;
    }

    /**
     * Gets the koodiValue of the osoiteKayttotarkoitus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOsoiteKayttotarkoitus() {
        return osoiteKayttotarkoitus;
    }

    /**
     * Sets the koodiValue of the osoiteKayttotarkoitus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOsoiteKayttotarkoitus(String value) {
        this.osoiteKayttotarkoitus = value;
    }

    /**
     * Gets the koodiValue of the osoiteMuuKaytto property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOsoiteMuuKaytto() {
        return osoiteMuuKaytto;
    }

    /**
     * Sets the koodiValue of the osoiteMuuKaytto property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOsoiteMuuKaytto(String value) {
        this.osoiteMuuKaytto = value;
    }

    /**
     * Gets the koodiValue of the osoiteTyyppi property.
     * 
     * @return
     *     possible object is
     *     {@link OsoiteTyyppi }
     *     
     */
    public OsoiteTyyppi getOsoiteTyyppi() {
        return osoiteTyyppi;
    }

    /**
     * Sets the koodiValue of the osoiteTyyppi property.
     * 
     * @param value
     *     allowed object is
     *     {@link OsoiteTyyppi }
     *     
     */
    public void setOsoiteTyyppi(OsoiteTyyppi value) {
        this.osoiteTyyppi = value;
    }

    /**
     * Gets the koodiValue of the postinumero property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPostinumero() {
        return postinumero;
    }

    /**
     * Sets the koodiValue of the postinumero property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPostinumero(String value) {
        this.postinumero = value;
    }

    /**
     * Gets the koodiValue of the postitoimipaikka property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPostitoimipaikka() {
        return postitoimipaikka;
    }

    /**
     * Sets the koodiValue of the postitoimipaikka property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPostitoimipaikka(String value) {
        this.postitoimipaikka = value;
    }

    /**
     * Gets the koodiValue of the ytjPaivitysPvm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getYtjPaivitysPvm() {
        return ytjPaivitysPvm;
    }

    /**
     * Sets the koodiValue of the ytjPaivitysPvm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setYtjPaivitysPvm(Date value) {
        this.ytjPaivitysPvm = value;
    }

}
