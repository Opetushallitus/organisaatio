package fi.vm.sade.organisaatio.dto.v2;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.*;

@XmlRootElement
@Schema(description = "Organisaation suppeat perustiedot")
public class OrganisaatioPerustietoSuppea implements Serializable {

    private final static long serialVersionUID = 100L;

    @Schema(description = "Organisaation oid", required = true)
    private String oid;

    @Schema(description = "Organisaation nimi", required = true)
    private Map<String, String> nimi = new HashMap<String, String>();

    @Schema(description = "Organisaation tyypit", required = true)
    private Set<OrganisaatioTyyppi> tyypit = new HashSet<>();

    @Schema(description = "Oppilaitoksen tyyppi", required = true)
    private String oppilaitostyyppi;

    private Collection<OrganisaatioPerustietoSuppea> children = new HashSet<>();

    public Collection<OrganisaatioPerustietoSuppea> getChildren() {
        return children;
    }

    public void setChildren(Collection<OrganisaatioPerustietoSuppea> children) {
        this.children = children;
    }

    public Map<String, String> getNimi() {
        return nimi;
    }

    public void setNimi(Map<String, String> nimi) {
        this.nimi = nimi;
    }

    public OrganisaatioPerustietoSuppea() {
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

    @Schema(description = "Organisaation tyypit", required = true)
    public Set<OrganisaatioTyyppi> getOrganisaatiotyypit() {
        if (tyypit == null) {
            return null;
        }
        if (tyypit.size() == 0) {
            return null;
        }
        return this.tyypit;
    }

    public void setOrganisaatiotyypit(Set<OrganisaatioTyyppi> tyypit) {
        this.tyypit = tyypit;
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

}
