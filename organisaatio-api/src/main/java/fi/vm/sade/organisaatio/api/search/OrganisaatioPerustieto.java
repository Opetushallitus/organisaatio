package fi.vm.sade.organisaatio.api.search;

import com.fasterxml.jackson.annotation.JsonFormat;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioStatus;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.util.OrganisaatioPerustietoUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.*;

@XmlRootElement
@ApiModel(value = "Organisaation perustiedot")
public class OrganisaatioPerustieto implements Serializable {

    private final static long serialVersionUID = 100L;

    @ApiModelProperty(value = "Organisaation oid", required = true)
    private String oid;

    @ApiModelProperty(value = "Aloituspäivämäärä", required = true)
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date alkuPvm;

    @ApiModelProperty(value = "Lakkautuspäivämäärä", required = true)
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date lakkautusPvm;

    @ApiModelProperty(value = "Tarkastuspäivämäärä", required = true)
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date tarkastusPvm;

    @ApiModelProperty(value = "Yläorganisaation oid", required = true)
    private String parentOid;

    @ApiModelProperty(value = "Yläorganisaation oid-polku", required = true)
    private String parentOidPath;

    @ApiModelProperty(value = "Y-tunnus", required = true)
    private String ytunnus;

    private String virastotunnus;

    private long aliorganisaatioMaara;

    @ApiModelProperty(value = "Oppilaitoksen koodi", required = true)
    private String oppilaitosKoodi;

    @ApiModelProperty(value = "Oppilaitoksen tyyppi", required = true)
    private String oppilaitostyyppi;

    @ApiModelProperty(value = "Toimipisteen koodi", required = true)
    private String toimipistekoodi;

    @ApiModelProperty(value = "Osuiko hakutuloksiin", required = true)
    private boolean match = false;

    @ApiModelProperty(value = "Organisaation nimi", required = true)
    private Map<String, String> nimi = new HashMap<String, String>();

    private Set<OrganisaatioTyyppi> tyypit = new HashSet<>();
    
    @ApiModelProperty(value = "Kielten URIt", required = true)
    private Set<String> kieletUris = new HashSet<>();
    
    @ApiModelProperty(value = "Kotipaikan URI", required = true)
    private String kotipaikkaUri;

    @ApiModelProperty(value = "Organisaation alaorganisaatiot", required = true)
    private Set<OrganisaatioPerustieto> children = new HashSet<>();

    public Set<OrganisaatioPerustieto> getChildren() {
        return children;
    }

    public void setChildren(Set<OrganisaatioPerustieto> children) {
        this.children = children;
    }

    public Map<String, String> getNimi() {
        return nimi;
    }

    public void setNimi(Map<String, String> nimi) {
        this.nimi = nimi;
    }

    public boolean isMatch() {
        return match;
    }

    public void setMatch(boolean match) {
        this.match = match;
    }

    public OrganisaatioPerustieto() {
        super();
    }

    /**
     * Gets the value of the oid property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getOid() {
        return oid;
    }

    /**
     * Sets the value of the oid property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setOid(String value) {
        this.oid = value;
    }

    /**
     * Gets the value of the alkuPvm property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public Date getAlkuPvm() {
        return alkuPvm;
    }

    /**
     * Sets the value of the alkuPvm property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setAlkuPvm(Date value) {
        this.alkuPvm = value;
    }

    /**
     * Gets the value of the lakkautusPvm property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public Date getLakkautusPvm() {
        return lakkautusPvm;
    }

    /**
     * Sets the value of the lakkautusPvm property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setLakkautusPvm(Date value) {
        this.lakkautusPvm = value;
    }

    /**
     * Gets the value of the parentOid property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getParentOid() {
        return parentOid;
    }

    /**
     * Sets the value of the parentOid property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setParentOid(String value) {
        this.parentOid = value;
    }

    /**
     * Gets the value of the parentOidPath property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getParentOidPath() {
        return parentOidPath;
    }

    /**
     * Sets the value of the parentOidPath property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setParentOidPath(String value) {
        this.parentOidPath = value;
    }

    /**
     * Gets the value of the ytunnus property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getYtunnus() {
        return ytunnus;
    }

    /**
     * Sets the value of the ytunnus property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setYtunnus(String value) {
        this.ytunnus = value;
    }

    /**
     * Gets the value of the virastoTunnus property.
     * 
     * @return possible object is {@link String }
     * 
     */
    @ApiModelProperty(value = "Virastotunnus", required = true)
    public String getVirastoTunnus() {
        return virastotunnus;
    }

    /**
     * Sets the value of the virastoTunnus property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setVirastoTunnus(String value) {
        this.virastotunnus = value;
    }

    /**
     * Gets the value of the aliOrganisaatioMaara property.
     * 
     */
    @ApiModelProperty(value = "Aliorganisaatioiden määrä", required = true)
    public long getAliOrganisaatioMaara() {
        return aliorganisaatioMaara;
    }

    /**
     * Sets the value of the aliOrganisaatioMaara property.
     * 
     */
    public void setAliOrganisaatioMaara(long value) {
        this.aliorganisaatioMaara = value;
    }

    /**
     * Gets the value of the oppilaitosKoodi property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getOppilaitosKoodi() {
        return oppilaitosKoodi;
    }

    /**
     * Sets the value of the oppilaitosKoodi property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setOppilaitosKoodi(String value) {
        this.oppilaitosKoodi = value;
    }

    /**
     * Gets the value of the oppilaitostyyppi property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getOppilaitostyyppi() {
        return oppilaitostyyppi;
    }

    /**
     * Sets the value of the oppilaitostyyppi property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setOppilaitostyyppi(String value) {
        this.oppilaitostyyppi = value;
    }
    /**
     * Gets the value of the toimipistekoodi property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getToimipistekoodi() {
        return toimipistekoodi;
    }
    /**
     * Sets the value of the oppilaitoskoodi property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setToimipistekoodi(String value) { this.toimipistekoodi = value; }

    @ApiModelProperty(value = "Organisaation tyypit", required = true)
    public Set<OrganisaatioTyyppi> getOrganisaatiotyypit() {
        if (tyypit == null) {
            tyypit = new HashSet<>();
        }
        return this.tyypit;
    }

    public void setOrganisaatiotyypit(Set<OrganisaatioTyyppi> organisaatiotyypit) {
        this.tyypit = organisaatiotyypit;
    }
    
    @ApiModelProperty(value = "Kielten URIt", required = true)
    public Set<String> getKieletUris() {
        if (kieletUris == null) {
            kieletUris = new HashSet<>();
        }
        return this.kieletUris;
    }

    public void setKieletUris(Set<String> kieletUris) {
        this.kieletUris = kieletUris;
    }

    @ApiModelProperty(value = "Kotipaikan URI", required = true)
    public String getKotipaikkaUri() {
        return kotipaikkaUri;
    }

    public void setKotipaikkaUri(String value) {
        this.kotipaikkaUri = value;
    }

    /**
     * Aseta organisaation nimi
     * 
     * @param targetLanguage
     *            "fi","en" tai "sv"
     * @param nimi
     */
    public void setNimi(String targetLanguage, String nimi) {
        this.nimi.put(targetLanguage, nimi);
    }

    /**
     * Palauta organisaation nimi, tai null jos ko kielellä ei löydy.
     * 
     * @param language
     *            "fi","en" tai "sv"
     * @return
     */
    public String getNimi(String language) {
        return this.nimi.get(language);
    }

    // Since all OrganisaatioStatus.POISTETTU are straight up removed from sorl index it's not possible case here
    public OrganisaatioStatus getStatus() {
        if(OrganisaatioPerustietoUtil.isPassive(this)) {
            return OrganisaatioStatus.PASSIIVINEN;
        }
        if(OrganisaatioPerustietoUtil.isSuunniteltu(this)) {
            return OrganisaatioStatus.SUUNNITELTU;
        }
        return OrganisaatioStatus.AKTIIVINEN;
    }

    public Date getTarkastusPvm() {
        return tarkastusPvm;
    }

    public void setTarkastusPvm(Date tarkastusPvm) {
        this.tarkastusPvm = tarkastusPvm;
    }
}
