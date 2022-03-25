package fi.vm.sade.organisaatio.resource.dto;

import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API used DTO, ie. "RDTO" for transmitting Organisaatio related data over
 * REST.
 *
 * .. well, actually "OrganisaatioDTO" and "Organisaatio" were already used and
 * I wanted to avoid confusion. :)
 *
 * "Natural key":
 * <ul>
 * <li>Koulutustoimija - y-tunnus</li>
 * <li>Oppilaitos - oppilaitosnumero</li>
 * <li>Toimipiste - oppilaitosnro + toimipisteenjärjestysnumero (konkatenoituna)
 * sekä yhkoulukoodi</li>
 * </ul>
 *
 * @author mlyly
 */
@Schema(description = "Organisaation tiedot")
public class OrganisaatioRDTO implements Serializable {

    private static final long serialVersionUID = -5019270750950297893L;

    private String _oid;

    private int _version;

    private Date _alkuPvm;

    private Date _lakkautusPvm;

    private String _ytjKieli;

    private Date _ytjPaivitysPvm;

    private Set<String> _kieletUris;

    private Set<String> _tyypit;

    private Set<String> _vuosiluokat;

    private Set<String> _ryhmatyypit;

    private Set<String> _kayttoryhmat;

    private Map<String, String> _nimi;


    private OrganisaatioRDTO _parentOrganisaatio;

    private List<OrganisaatioNimiRDTO> _nimet;

    private String _status;

    private String _maaUri;

    private String _domainNimi;

    private String _kotipaikkaUri;

    private String _oppilaitosKoodi;

    private String _oppilaitosTyyppiUri;

    private String _yTunnus;

    private String _toimipistekoodi;

    private String _yritysmuoto;

    private String _puhelinnumero; // from List of Yhteystietos

    private String _wwwOsoite; // from List of Yhteystietos

    private String _emailOsoite; // from List of Yhteystietos

    private Map<String, String> _postiosoite;

    private Map<String, String> _kayntiosoite;

    private Set<Map<String, String>> _yhteystiedot;

    private String _kuvaus;

    private Map<String, String> _kuvaus2;

    private String _parentOid;

    private String _parentOidPath;

    private OrganisaatioMetaDataRDTO _metadata;

    private String yhteishaunKoulukoodi;

    private Set<Map<String, String>> _yhteystietoArvos = null;
    private String _virastoTunnus;
    private String _opetuspisteenJarjNro;

    private Timestamp _tarkastusPvm; // täytyy olla Timestamp jotta päivityksen vastauksessa formaatti on oikea

    @Schema(description = "Organisaation oid", required = true)
    public String getOid() {
        return _oid;
    }

    public void setOid(String _oid) {
        this._oid = _oid;
    }

    @Schema(description = "Versio", required = true)
    public int getVersion() {
        return _version;
    }

    public void setVersion(int _version) {
        this._version = _version;
    }

    @Schema(description = "Alkamispäivämäärä", required = true)
    public Date getAlkuPvm() {
        return _alkuPvm;
    }

    public void setAlkuPvm(Date _alkuPvm) {
        this._alkuPvm = _alkuPvm;
    }

    @Schema(description = "Lakkautuspäivämäärä", required = true)
    public Date getLakkautusPvm() {
        return _lakkautusPvm;
    }

    public void setLakkautusPvm(Date _lakkautusPvm) {
        this._lakkautusPvm = _lakkautusPvm;
    }

    @Schema(description = "Kieli, jolla YTJ:stä haetut tiedot on päivitetty", required = true)
    public String getYTJKieli() {
        return _ytjKieli;
    }

    public void setYTJKieli(String _ytjKieli) {
        this._ytjKieli = _ytjKieli;
    }

    @Schema(description = "YTJ:n päivityspäivämäärä", required = true)
    public Date getYTJPaivitysPvm() {
        return _ytjPaivitysPvm;
    }

    public void setYTJPaivitysPvm(Date _ytjPaivitysPvm) {
        this._ytjPaivitysPvm = _ytjPaivitysPvm;
    }

    @Schema(description = "Kielten URIt", required = true)
    public Set<String> getKieletUris() {
        if (_kieletUris == null) {
            _kieletUris = new HashSet<>();
        }
        return _kieletUris;
    }

    public void setKieletUris(Set<String> _kieletUris) {
        this._kieletUris = _kieletUris;
    }

    @Schema(description = "Maan URI", required = true)
    public String getMaaUri() {
        return _maaUri;
    }

    public void setMaaUri(String _maaUri) {
        this._maaUri = _maaUri;
    }

    @Schema(description = "Domain", required = true)
    public String getDomainNimi() {
        return _domainNimi;
    }

    public void setDomainNimi(String _domainNimi) {
        this._domainNimi = _domainNimi;
    }

    @Schema(description = "Kotipaikan URI", required = true)
    public String getKotipaikkaUri() {
        return _kotipaikkaUri;
    }

    public void setKotipaikkaUri(String _kotipaikkaUri) {
        this._kotipaikkaUri = _kotipaikkaUri;
    }

    @Schema(description = "Nimi", required = true)
    public Map<String, String> getNimi() {
        if (_nimi == null) {
            _nimi = new HashMap<>();
        }
        if (_parentOrganisaatio == null) {
            return _nimi;
        }
        Map<String, String> parentName = _parentOrganisaatio.getNimi();
        return _nimi.keySet().stream().collect(Collectors.toMap(e -> e, e -> {
            String parentNimi = parentName.getOrDefault(e, "");
            String parentNimiWithSep = String.format("%s, ", parentName.getOrDefault(e, ""));
            String nimi = _nimi.get(e);
            return nimi.equals(parentNimi) ? nimi : String.format("%s%s", parentNimiWithSep, nimi);
        }));
    }
    public void setParentOrganisaatio(OrganisaatioRDTO parent) {
        _parentOrganisaatio = parent;
    }
    public void setNimi(Map<String, String> _nimi) {
        this._nimi = _nimi;
    }

    @Schema(description = "Organisaation nimihistoria", required = true)
    public List<OrganisaatioNimiRDTO> getNimet() {
         if (_nimet == null) {
            _nimet = new ArrayList<>();
        }
        return _nimet;
    }

    public void setNimet(List<OrganisaatioNimiRDTO> _nimet) {
        this._nimet = _nimet;
    }

    @Schema(description = "Oppilaitoksen koodi", required = true)
    public String getOppilaitosKoodi() {
        return _oppilaitosKoodi;
    }

    public void setOppilaitosKoodi(String _oppilaitosKoodi) {
        this._oppilaitosKoodi = _oppilaitosKoodi;
    }

    @Schema(description = "Oppilaitoksen tyypin URI", required = true)
    public String getOppilaitosTyyppiUri() {
        return _oppilaitosTyyppiUri;
    }

    public void setOppilaitosTyyppiUri(String _oppilaitosTyyppiUri) {
        this._oppilaitosTyyppiUri = _oppilaitosTyyppiUri;
    }

    @Schema(description = "Y-tunnus", required = true)
    public String getYTunnus() {
        return _yTunnus;
    }

    public void setYTunnus(String _yTunnus) {
        this._yTunnus = _yTunnus;
    }

    @Schema(description = "Tyypit", required = true)
    public Set<String> getTyypit() {
        if (_tyypit == null) {
            _tyypit = new HashSet<>();
        }
        return _tyypit;
    }

    public void setTyypit(Set<String> _tyypit) {
        this._tyypit = _tyypit;
    }

    @Schema(description = "Toimipisteen koodi", required = true)
    public String getToimipistekoodi() {
        return _toimipistekoodi;
    }

    public void setToimipistekoodi(String _toimipistekoodi) {
        this._toimipistekoodi = _toimipistekoodi;
    }

    @Schema(description = "Yritysmuoto", required = true)
    public String getYritysmuoto() {
        return _yritysmuoto;
    }

    public void setYritysmuoto(String _yritysmuoto) {
        this._yritysmuoto = _yritysmuoto;
    }

    @Schema(description = "Vuosiluokat", required = true)
    public Set<String> getVuosiluokat() {
        if (_vuosiluokat == null) {
            _vuosiluokat = new HashSet<>();
        }
        return _vuosiluokat;
    }

    public void setVuosiluokat(Set<String> _vuosiluokat) {
        this._vuosiluokat = _vuosiluokat;
    }

    @Schema(description = "Ryhmatyypit", required = true)
    public Set<String> getRyhmatyypit() {
        if (_ryhmatyypit == null) {
            _ryhmatyypit = new HashSet<>();
        }
        return _ryhmatyypit;
    }

    public void setRyhmatyypit(Set<String> _ryhmatyypit) {
        this._ryhmatyypit = _ryhmatyypit;
    }

    @Schema(description = "Kayttoryhmat", required = true)
    public Set<String> getKayttoryhmat() {
        if (_kayttoryhmat == null) {
            _kayttoryhmat = new HashSet<>();
        }
        return _kayttoryhmat;
    }

    public void setKayttoryhmat(Set<String> _kayttoryhmat) {
        this._kayttoryhmat = _kayttoryhmat;
    }

    @Schema(description = "Käyntiosoite", required = true)
    public Map<String, String> getKayntiosoite() {
        if (_kayntiosoite == null) {
            _kayntiosoite = new HashMap<String, String>();
        }
        return _kayntiosoite;
    }

    public void setKayntiosoite(Map<String, String> _kayntiosoite) {
        this._kayntiosoite = _kayntiosoite;
    }

    @Schema(description = "Postiosoite", required = true)
    public Map<String, String> getPostiosoite() {
        if (_postiosoite == null) {
            _postiosoite = new HashMap<String, String>();
        }
        return _postiosoite;
    }

    public void setPostiosoite(Map<String, String> _postiosoite) {
        this._postiosoite = _postiosoite;
    }

    @Schema(description = "Kuvaus", required = true)
    public String getKuvaus() {
        return _kuvaus;
    }

    public void setKuvaus(String _kuvaus) {
        this._kuvaus = _kuvaus;
    }

    @Schema(description = "Toinen kuvaus", required = true)
    public Map<String, String> getKuvaus2() {
        if (_kuvaus2 == null) {
            _kuvaus2 = new HashMap<String, String>();
        }
        return _kuvaus2;
    }

    public void setKuvaus2(Map<String, String> _kuvaus2) {
        this._kuvaus2 = _kuvaus2;
    }

    @Schema(description = "Yläorganisaation oid", required = true)
    public String getParentOid() {
        return _parentOid;
    }

    public void setParentOid(String _parentOid) {
        this._parentOid = _parentOid;
    }

    @Schema(description = "Yläorganisaation oid-polku", required = true)
    public String getParentOidPath() {
        return _parentOidPath;
    }

    public void setParentOidPath(String _parentOidPath) {
        this._parentOidPath = _parentOidPath;
    }

    @Schema(description = "Metatiedot", required = true)
    public OrganisaatioMetaDataRDTO getMetadata() {
        return _metadata;
    }

    public void setMetadata(OrganisaatioMetaDataRDTO _metadata) {
        this._metadata = _metadata;
    }

    /**
     * @return
     * @deprecated Do not use this method! Use getYhteystiedot() instead!
     */
    @Deprecated
    @Schema(description = "Sähköpostiosoite", required = true)
    public String getEmailOsoite() {
        return _emailOsoite;
    }

    /**
     * @param _emailOsoite
     * @deprecated Do not use this method! Use setYhteystiedot() instead!
     */
    @Deprecated
    public void setEmailOsoite(String _emailOsoite) {
        this._emailOsoite = _emailOsoite;
    }

    /**
     * @return
     * @deprecated Do not use this method! Use getYhteystiedot() instead!
     */
    @Deprecated
    @Schema(description = "Puhelinnumero", required = true)
    public String getPuhelinnumero() {
        return _puhelinnumero;
    }

    /**
     * @param _puhelinnumero
     * @deprecated Do not use this method! Use setYhteystiedot() instead!
     */
    @Deprecated
    public void setPuhelinnumero(String _puhelinnumero) {
        this._puhelinnumero = _puhelinnumero;
    }

    /**
     * @return
     * @deprecated Do not use this method! Use getYhteystiedot() instead!
     */
    @Deprecated
    @Schema(description = "WWW-osoite", required = true)
    public String getWwwOsoite() {
        return _wwwOsoite;
    }

    /**
     * @param _wwwOsoite
     * @deprecated Do not use this method! Use setYhteystiedot() instead!
     */
    @Deprecated
    public void setWwwOsoite(String _wwwOsoite) {
        this._wwwOsoite = _wwwOsoite;
    }

    @Schema(description = "Yhteishaun koulukoodi", required = true)
    @Deprecated
    public String getYhteishaunKoulukoodi() {
        return yhteishaunKoulukoodi;
    }

    @Deprecated
    public void setYhteishaunKoulukoodi(String yhteishaunKoulukoodi) {
        this.yhteishaunKoulukoodi = yhteishaunKoulukoodi;
    }

    @Schema(description = "Yhteystiedot", required = true)
    public Set<Map<String, String>> getYhteystietoArvos() {
        return _yhteystietoArvos;
    }

    public void setYhteystietoArvos(Set<Map<String, String>> yhteystietoArvos) {
        this._yhteystietoArvos = yhteystietoArvos;
    }

    public String getVirastoTunnus() {
        return _virastoTunnus;
    }

    public void setVirastoTunnus(String _virastotunnus) {
        this._virastoTunnus = _virastotunnus;
    }

    public String getOpetuspisteenJarjNro() {
        return _opetuspisteenJarjNro;
    }

    public void setOpetuspisteenJarjNro(String _opetuspisteenJarjNro) {
        this._opetuspisteenJarjNro = _opetuspisteenJarjNro;
    }

    public Timestamp getTarkastusPvm() {
        return _tarkastusPvm;
    }

    public void setTarkastusPvm(Timestamp _tarkastusPvm) {
        this._tarkastusPvm = _tarkastusPvm;
    }

    public Set<Map<String, String>> getYhteystiedot() {
        if (_yhteystiedot == null) {
            _yhteystiedot = new HashSet<>();
        }
        return _yhteystiedot;
    }

    public void setYhteystiedot(Set<Map<String, String>> _yhteystiedot) {
        this._yhteystiedot = _yhteystiedot;
    }

    public void addYhteystieto(Map<String, String> yhtMap) {
        getYhteystiedot().add(yhtMap);
    }

    /**
     * @return the _status
     */
    public String getStatus() {
        return _status;
    }

    /**
     * @param _status the _status to set
     */
    public void setStatus(String _status) {
        this._status = _status;
    }
}
