package fi.vm.sade.organisaatio.dto.v4;

import com.fasterxml.jackson.annotation.JsonFormat;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioStatus;
import fi.vm.sade.organisaatio.api.util.OrganisaatioPerustietoUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.*;

@XmlRootElement
@ApiModel(value = "Organisaation perustiedot v4")
public class OrganisaatioPerustietoV4 implements Serializable {

    private final static long serialVersionUID = 100L;

    @ApiModelProperty(value = "Organisaation oid", required = true)
    private String oid;

    @ApiModelProperty(value = "Aloituspäivämäärä", required = true)
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date alkuPvm;

    @ApiModelProperty(value = "Lakkautuspäivämäärä", required = true)
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date lakkautusPvm;

    @ApiModelProperty(value = "Yläorganisaation oid", required = true)
    private String parentOid;

    @ApiModelProperty(value = "Yläorganisaation oid-polku", required = true)
    private String parentOidPath;

    @ApiModelProperty(value = "Y-tunnus", required = true)
    private String ytunnus;

    private String virastotunnus;

    private int aliorganisaatioMaara;

    @ApiModelProperty(value = "Oppilaitoksen koodi", required = true)
    private String oppilaitosKoodi;

    @ApiModelProperty(value = "Oppilaitoksen tyyppi", required = true)
    private String oppilaitostyyppi;

    @ApiModelProperty(value = "Toimipisteen koodi", required = true)
    private String toimipistekoodi;

    @ApiModelProperty(value = "Osuiko hakutuloksiin", required = true)
    private boolean match = false;

    @ApiModelProperty(value = "Organisaation nimi", required = true)
    private Map<String, String> nimi = new HashMap<>();

    // Tyypit koodiarvoina
    private List<String> tyypit = new ArrayList<>();

    @ApiModelProperty(value = "Kielten URIt", required = true)
    private List<String> kieletUris = new ArrayList<>();

    @ApiModelProperty(value = "Kotipaikan URI", required = true)
    private String kotipaikkaUri;

    @ApiModelProperty(value = "Organisaation alaorganisaatiot", required = true)
    private List<OrganisaatioPerustietoV4> children = new ArrayList<>();

    public List<OrganisaatioPerustietoV4> getChildren() {
        return children;
    }

    public void setChildren(List<OrganisaatioPerustietoV4> children) {
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

    public OrganisaatioPerustietoV4() {
        super();
    }

    /**
     * Gets the koodiValue of the oid property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getOid() {
        return oid;
    }

    /**
     * Sets the koodiValue of the oid property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setOid(String value) {
        this.oid = value;
    }

    /**
     * Gets the koodiValue of the alkuPvm property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public Date getAlkuPvm() {
        return alkuPvm;
    }

    /**
     * Sets the koodiValue of the alkuPvm property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setAlkuPvm(Date value) {
        this.alkuPvm = value;
    }

    /**
     * Gets the koodiValue of the lakkautusPvm property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public Date getLakkautusPvm() {
        return lakkautusPvm;
    }

    /**
     * Sets the koodiValue of the lakkautusPvm property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setLakkautusPvm(Date value) {
        this.lakkautusPvm = value;
    }

    /**
     * Gets the koodiValue of the parentOid property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getParentOid() {
        return parentOid;
    }

    /**
     * Sets the koodiValue of the parentOid property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setParentOid(String value) {
        this.parentOid = value;
    }

    /**
     * Gets the koodiValue of the parentOidPath property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getParentOidPath() {
        return parentOidPath;
    }

    /**
     * Sets the koodiValue of the parentOidPath property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setParentOidPath(String value) {
        this.parentOidPath = value;
    }

    /**
     * Gets the koodiValue of the ytunnus property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getYtunnus() {
        return ytunnus;
    }

    /**
     * Sets the koodiValue of the ytunnus property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setYtunnus(String value) {
        this.ytunnus = value;
    }

    /**
     * Gets the koodiValue of the virastoTunnus property.
     * 
     * @return possible object is {@link String }
     * 
     */
    @ApiModelProperty(value = "Virastotunnus", required = true)
    public String getVirastoTunnus() {
        return virastotunnus;
    }

    /**
     * Sets the koodiValue of the virastoTunnus property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setVirastoTunnus(String value) {
        this.virastotunnus = value;
    }

    /**
     * Gets the koodiValue of the aliOrganisaatioMaara property.
     * 
     */
    @ApiModelProperty(value = "Aliorganisaatioiden määrä", required = true)
    public int getAliOrganisaatioMaara() {
        return aliorganisaatioMaara;
    }

    /**
     * Sets the koodiValue of the aliOrganisaatioMaara property.
     * 
     */
    public void setAliOrganisaatioMaara(int value) {
        this.aliorganisaatioMaara = value;
    }

    /**
     * Gets the koodiValue of the oppilaitosKoodi property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getOppilaitosKoodi() {
        return oppilaitosKoodi;
    }

    /**
     * Sets the koodiValue of the oppilaitosKoodi property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setOppilaitosKoodi(String value) {
        this.oppilaitosKoodi = value;
    }

    /**
     * Gets the koodiValue of the oppilaitostyyppi property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getOppilaitostyyppi() {
        return oppilaitostyyppi;
    }

    /**
     * Sets the koodiValue of the oppilaitostyyppi property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setOppilaitostyyppi(String value) {
        this.oppilaitostyyppi = value;
    }
    /**
     * Gets the koodiValue of the toimipistekoodi property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getToimipistekoodi() {
        return toimipistekoodi;
    }
    /**
     * Sets the koodiValue of the oppilaitoskoodi property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setToimipistekoodi(String value) { this.toimipistekoodi = value; }

    @ApiModelProperty(value = "Organisaation tyypit koodiarvoina", required = true)
    public List<String> getOrganisaatiotyypit() {
        if (tyypit == null) {
            tyypit = new ArrayList<>();
        }
        return this.tyypit;
    }

    public void setOrganisaatiotyypit(List<String> organisaatiotyypit) {
        this.tyypit = organisaatiotyypit;
    }

    @ApiModelProperty(value = "Kielten URIt", required = true)
    public List<String> getKieletUris() {
        if (kieletUris == null) {
            kieletUris = new ArrayList<>();
        }
        return this.kieletUris;
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
}
