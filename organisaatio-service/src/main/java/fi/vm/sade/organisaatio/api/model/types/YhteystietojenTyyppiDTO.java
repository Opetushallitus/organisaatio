
package fi.vm.sade.organisaatio.api.model.types;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


public class YhteystietojenTyyppiDTO implements Serializable {

    private static final long serialVersionUID = 100L;
    protected long version;
    protected String oid;
    protected MonikielinenTekstiTyyppi nimi;
    private Set<String> sovellettavatOrganisaatios;
    private Set<YhteystietoElementtiDTO> allLisatietokenttas;
    private Set<String> sovellettavatOppilaitostyyppis;

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
     * Gets the value of the oid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOid() {
        return oid;
    }

    /**
     * Sets the value of the oid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOid(String value) {
        this.oid = value;
    }

    /**
     * Gets the value of the nimi property.
     * 
     * @return
     *     possible object is
     *     {@link MonikielinenTekstiTyyppi }
     *     
     */
    public MonikielinenTekstiTyyppi getNimi() {
        return nimi;
    }

    /**
     * Sets the value of the nimi property.
     * 
     * @param value
     *     allowed object is
     *     {@link MonikielinenTekstiTyyppi }
     *     
     */
    public void setNimi(MonikielinenTekstiTyyppi value) {
        this.nimi = value;
    }

    /**
     * Gets the value of the sovellettavatOrganisaatios property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sovellettavatOrganisaatios property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSovellettavatOrganisaatios().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public Set<String> getSovellettavatOrganisaatios() {
        if (sovellettavatOrganisaatios == null) {
            sovellettavatOrganisaatios = new HashSet<>();
        }
        return this.sovellettavatOrganisaatios;
    }

    /**
     * Gets the value of the allLisatietokenttas property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the allLisatietokenttas property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAllLisatietokenttas().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link YhteystietoElementtiDTO }
     * 
     * 
     */
    public Set<YhteystietoElementtiDTO> getAllLisatietokenttas() {
        if (allLisatietokenttas == null) {
            allLisatietokenttas = new HashSet<>();
        }
        return this.allLisatietokenttas;
    }

    /**
     * Gets the value of the sovellettavatOppilaitostyyppis property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sovellettavatOppilaitostyyppis property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSovellettavatOppilaitostyyppis().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public Set<String> getSovellettavatOppilaitostyyppis() {
        if (sovellettavatOppilaitostyyppis == null) {
            sovellettavatOppilaitostyyppis = new HashSet<>();
        }
        return this.sovellettavatOppilaitostyyppis;
    }

}
