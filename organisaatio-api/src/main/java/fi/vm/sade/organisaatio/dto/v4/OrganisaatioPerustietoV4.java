package fi.vm.sade.organisaatio.dto.v4;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioStatus;
import fi.vm.sade.organisaatio.api.util.OrganisaatioPerustietoUtil;
import fi.vm.sade.organisaatio.api.views.Views;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.*;

@XmlRootElement
@Schema(description = "Organisaation perustiedot v4")
@JsonView(Views.None.class)
@JsonPropertyOrder(alphabetic=true)
public class OrganisaatioPerustietoV4 implements Serializable {

    private final static long serialVersionUID = 100L;

    @Schema(description = "Organisaation oid", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonView(Views.Basic.class)
    private String oid;

    @Schema(description = "Aloituspäivämäärä", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date alkuPvm;

    @Schema(description = "Lakkautuspäivämäärä", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date lakkautusPvm;

    @JsonIgnore
    @Getter
    @Setter
    private boolean maskingActive;

    @Schema(description = "TarkastusPäivämäärä", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date tarkastusPvm;

    @Schema(description = "Yläorganisaation oid", requiredMode = Schema.RequiredMode.REQUIRED)
    private String parentOid;

    @Schema(description = "Yläorganisaation oid-polku", requiredMode = Schema.RequiredMode.REQUIRED)
    private String parentOidPath;

    @Schema(description = "Y-tunnus", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ytunnus;

    private String virastotunnus;

    private int aliorganisaatioMaara;

    @Schema(description = "Oppilaitoksen koodi", requiredMode = Schema.RequiredMode.REQUIRED)
    private String oppilaitosKoodi;

    @Schema(description = "Oppilaitoksen tyyppi", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonView(Views.Tyyppi.class)
    private String oppilaitostyyppi;

    @Schema(description = "Toimipisteen koodi", requiredMode = Schema.RequiredMode.REQUIRED)
    private String toimipistekoodi;

    @Schema(description = "Osuiko hakutuloksiin", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean match = false;

    @Schema(description = "Organisaation nimi", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonView(Views.Nimi.class)
    private Map<String, String> nimi = new HashMap<>();
    private Map<String, String> lyhytNimi = new HashMap<>();

    // Tyypit koodiarvoina
    @JsonView(Views.Tyyppi.class)
    private Set<String> tyypit = new LinkedHashSet<>();

    @Schema(description = "Kielten URIt", requiredMode = Schema.RequiredMode.REQUIRED)
    private Set<String> kieletUris = new LinkedHashSet<>();

    @Schema(description = "Kotipaikan URI", requiredMode = Schema.RequiredMode.REQUIRED)
    private String kotipaikkaUri;

    @Schema(description = "Organisaation alaorganisaatiot", requiredMode = Schema.RequiredMode.REQUIRED)
    private Set<OrganisaatioPerustietoV4> children = new LinkedHashSet<>();

    public Set<OrganisaatioPerustietoV4> getChildren() {
        return children;
    }

    @Schema(description = "Organisaation alaorganisaatiot react tableen", requiredMode = Schema.RequiredMode.REQUIRED)
    private Set<OrganisaatioPerustietoV4> subRows = new LinkedHashSet<>();

    public Set<OrganisaatioPerustietoV4> getSubRows() {
        return subRows;
    }

    public void setChildren(Set<OrganisaatioPerustietoV4> children) {
        this.children = children;
        this.subRows = children;
    }

    public Map<String, String> getNimi() {
        return nimi;
    }

    public void setNimi(Map<String, String> nimi) {
        this.nimi = nimi;
    }
    public void setLyhytNimi(Map<String, String> lyhytNimi) {
        this.lyhytNimi = lyhytNimi;
    }

    public Map<String, String> getLyhytNimi() {
        return lyhytNimi;
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
    @Schema(description = "Virastotunnus", requiredMode = Schema.RequiredMode.REQUIRED)
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
    @Schema(description = "Aliorganisaatioiden määrä", requiredMode = Schema.RequiredMode.REQUIRED)
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

    @Schema(description = "Organisaation tyypit koodiarvoina", requiredMode = Schema.RequiredMode.REQUIRED)
    public Set<String> getOrganisaatiotyypit() {
        if (tyypit == null) {
            tyypit = new HashSet<>();
        }
        return this.tyypit;
    }

    public void setOrganisaatiotyypit(Set<String> organisaatiotyypit) {
        this.tyypit = organisaatiotyypit;
    }

    @Schema(description = "Kielten URIt", requiredMode = Schema.RequiredMode.REQUIRED)
    public Set<String> getKieletUris() {
        if (kieletUris == null) {
            kieletUris = new HashSet<>();
        }
        return this.kieletUris;
    }

    public void setKieletUris(Set<String> kieletUris) {
        this.kieletUris = kieletUris;
    }

    @Schema(description = "Kotipaikan URI", requiredMode = Schema.RequiredMode.REQUIRED)
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
