/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 */
package fi.vm.sade.organisaatio.resource.dto;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.*;

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
@ApiModel(value = "Organisaation tiedot")
public class OrganisaatioRDTO implements Serializable {

    private static final long serialVersionUID = -5019270750950297893L;

    private String _oid;

    private int _version;

    private Date _alkuPvm;

    private Date _lakkautusPvm;

    private Date _ytjPaivitysPvm;

    private List<String> _kieletUris;

    private List<String> _tyypit;

    private List<String> _vuosiluokat;

    private List<String> _ryhmatyypit;

    private List<String> _kayttoryhmat;

    private Map<String, String> _nimi;

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

    private String _faksinumero; // from List of Yhteystietos

    private String _wwwOsoite; // from List of Yhteystietos

    private String _emailOsoite; // from List of Yhteystietos

    private Map<String, String> _postiosoite;

    private Map<String, String> _kayntiosoite;

    private List<Map<String, String>> _yhteystiedot;

    private String _kuvaus;

    private Map<String, String> _kuvaus2;

    private String _parentOid;

    private String _parentOidPath;

    private OrganisaatioMetaDataRDTO _metadata;

    private String yhteishaunKoulukoodi;

    private List<Map<String, String>> _yhteystietoArvos = null;
    private String _virastoTunnus;
    private String _opetuspisteenJarjNro;

    @ApiModelProperty(value = "Organisaation oid", required = true)
    public String getOid() {
        return _oid;
    }

    public void setOid(String _oid) {
        this._oid = _oid;
    }

    @ApiModelProperty(value = "Versio", required = true)
    public int getVersion() {
        return _version;
    }

    public void setVersion(int _version) {
        this._version = _version;
    }

    @ApiModelProperty(value = "Alkamispäivämäärä", required = true)
    public Date getAlkuPvm() {
        return _alkuPvm;
    }

    public void setAlkuPvm(Date _alkuPvm) {
        this._alkuPvm = _alkuPvm;
    }

    @ApiModelProperty(value = "Lakkautuspäivämäärä", required = true)
    public Date getLakkautusPvm() {
        return _lakkautusPvm;
    }

    public void setLakkautusPvm(Date _lakkautusPvm) {
        this._lakkautusPvm = _lakkautusPvm;
    }

    @ApiModelProperty(value = "YTJ:n päivityspäivämäärä", required = true)
    public Date getYTJPaivitysPvm() {
        return _ytjPaivitysPvm;
    }

    public void setYTJPaivitysPvm(Date _ytjPaivitysPvm) {
        this._ytjPaivitysPvm = _ytjPaivitysPvm;
    }

    @ApiModelProperty(value = "Kielten URIt", required = true)
    public List<String> getKieletUris() {
        if (_kieletUris == null) {
            _kieletUris = new ArrayList<String>();
        }
        return _kieletUris;
    }

    public void setKieletUris(List<String> _kieletUris) {
        this._kieletUris = _kieletUris;
    }

    @ApiModelProperty(value = "Maan URI", required = true)
    public String getMaaUri() {
        return _maaUri;
    }

    public void setMaaUri(String _maaUri) {
        this._maaUri = _maaUri;
    }

    @ApiModelProperty(value = "Domain", required = true)
    public String getDomainNimi() {
        return _domainNimi;
    }

    public void setDomainNimi(String _domainNimi) {
        this._domainNimi = _domainNimi;
    }

    @ApiModelProperty(value = "Kotipaikan URI", required = true)
    public String getKotipaikkaUri() {
        return _kotipaikkaUri;
    }

    public void setKotipaikkaUri(String _kotipaikkaUri) {
        this._kotipaikkaUri = _kotipaikkaUri;
    }

    @ApiModelProperty(value = "Nimi", required = true)
    public Map<String, String> getNimi() {
        if (_nimi == null) {
            _nimi = new HashMap<String, String>();
        }
        return _nimi;
    }

    public void setNimi(Map<String, String> _nimi) {
        this._nimi = _nimi;
    }

    @ApiModelProperty(value = "Organisaation nimihistoria", required = true)
    public List<OrganisaatioNimiRDTO> getNimet() {
         if (_nimet == null) {
            _nimet = new ArrayList<OrganisaatioNimiRDTO>();
        }
        return _nimet;
    }

    public void setNimet(List<OrganisaatioNimiRDTO> _nimet) {
        this._nimet = _nimet;
    }

    @ApiModelProperty(value = "Oppilaitoksen koodi", required = true)
    public String getOppilaitosKoodi() {
        return _oppilaitosKoodi;
    }

    public void setOppilaitosKoodi(String _oppilaitosKoodi) {
        this._oppilaitosKoodi = _oppilaitosKoodi;
    }

    @ApiModelProperty(value = "Oppilaitoksen tyypin URI", required = true)
    public String getOppilaitosTyyppiUri() {
        return _oppilaitosTyyppiUri;
    }

    public void setOppilaitosTyyppiUri(String _oppilaitosTyyppiUri) {
        this._oppilaitosTyyppiUri = _oppilaitosTyyppiUri;
    }

    @ApiModelProperty(value = "Y-tunnus", required = true)
    public String getYTunnus() {
        return _yTunnus;
    }

    public void setYTunnus(String _yTunnus) {
        this._yTunnus = _yTunnus;
    }

    @ApiModelProperty(value = "Tyypit", required = true)
    public List<String> getTyypit() {
        if (_tyypit == null) {
            _tyypit = new ArrayList<String>();
        }
        return _tyypit;
    }

    public void setTyypit(List<String> _tyypit) {
        this._tyypit = _tyypit;
    }

    @ApiModelProperty(value = "Toimipisteen koodi", required = true)
    public String getToimipistekoodi() {
        return _toimipistekoodi;
    }

    public void setToimipistekoodi(String _toimipistekoodi) {
        this._toimipistekoodi = _toimipistekoodi;
    }

    @ApiModelProperty(value = "Yritysmuoto", required = true)
    public String getYritysmuoto() {
        return _yritysmuoto;
    }

    public void setYritysmuoto(String _yritysmuoto) {
        this._yritysmuoto = _yritysmuoto;
    }

    @ApiModelProperty(value = "Vuosiluokat", required = true)
    public List<String> getVuosiluokat() {
        if (_vuosiluokat == null) {
            _vuosiluokat = new ArrayList<String>();
        }
        return _vuosiluokat;
    }

    public void setVuosiluokat(List<String> _vuosiluokat) {
        this._vuosiluokat = _vuosiluokat;
    }

    @ApiModelProperty(value = "Ryhmatyypit", required = true)
    public List<String> getRyhmatyypit() {
        if (_ryhmatyypit == null) {
            _ryhmatyypit = new ArrayList<String>();
        }
        return _ryhmatyypit;
    }

    public void setRyhmatyypit(List<String> _ryhmatyypit) {
        this._ryhmatyypit = _ryhmatyypit;
    }

    @ApiModelProperty(value = "Kayttoryhmat", required = true)
    public List<String> getKayttoryhmat() {
        if (_kayttoryhmat == null) {
            _kayttoryhmat = new ArrayList<String>();
        }
        return _kayttoryhmat;
    }

    public void setKayttoryhmat(List<String> _kayttoryhmat) {
        this._kayttoryhmat = _kayttoryhmat;
    }

    @ApiModelProperty(value = "Käyntiosoite", required = true)
    public Map<String, String> getKayntiosoite() {
        if (_kayntiosoite == null) {
            _kayntiosoite = new HashMap<String, String>();
        }
        return _kayntiosoite;
    }

    public void setKayntiosoite(Map<String, String> _kayntiosoite) {
        this._kayntiosoite = _kayntiosoite;
    }

    @ApiModelProperty(value = "Postiosoite", required = true)
    public Map<String, String> getPostiosoite() {
        if (_postiosoite == null) {
            _postiosoite = new HashMap<String, String>();
        }
        return _postiosoite;
    }

    public void setPostiosoite(Map<String, String> _postiosoite) {
        this._postiosoite = _postiosoite;
    }

    @ApiModelProperty(value = "Kuvaus", required = true)
    public String getKuvaus() {
        return _kuvaus;
    }

    public void setKuvaus(String _kuvaus) {
        this._kuvaus = _kuvaus;
    }

    @ApiModelProperty(value = "Toinen kuvaus", required = true)
    public Map<String, String> getKuvaus2() {
        if (_kuvaus2 == null) {
            _kuvaus2 = new HashMap<String, String>();
        }
        return _kuvaus2;
    }

    public void setKuvaus2(Map<String, String> _kuvaus2) {
        this._kuvaus2 = _kuvaus2;
    }

    @ApiModelProperty(value = "Yläorganisaation oid", required = true)
    public String getParentOid() {
        return _parentOid;
    }

    public void setParentOid(String _parentOid) {
        this._parentOid = _parentOid;
    }

    @ApiModelProperty(value = "Yläorganisaation oid-polku", required = true)
    public String getParentOidPath() {
        return _parentOidPath;
    }

    public void setParentOidPath(String _parentOidPath) {
        this._parentOidPath = _parentOidPath;
    }

    @ApiModelProperty(value = "Metatiedot", required = true)
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
    @ApiModelProperty(value = "Sähköpostiosoite", required = true)
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
    @ApiModelProperty(value = "Faxin numero", required = true)
    public String getFaksinumero() {
        return _faksinumero;
    }

    /**
     * @param _faksinumero
     * @deprecated Do not use this method! Use setYhteystiedot() instead!
     */
    @Deprecated
    public void setFaksinumero(String _faksinumero) {
        this._faksinumero = _faksinumero;
    }

    /**
     * @return
     * @deprecated Do not use this method! Use getYhteystiedot() instead!
     */
    @Deprecated
    @ApiModelProperty(value = "Puhelinnumero", required = true)
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
    @ApiModelProperty(value = "WWW-osoite", required = true)
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

    @ApiModelProperty(value = "Yhteishaun koulukoodi", required = true)
    @Deprecated
    public String getYhteishaunKoulukoodi() {
        return yhteishaunKoulukoodi;
    }

    @Deprecated
    public void setYhteishaunKoulukoodi(String yhteishaunKoulukoodi) {
        this.yhteishaunKoulukoodi = yhteishaunKoulukoodi;
    }

    @ApiModelProperty(value = "Yhteystiedot", required = true)
    public List<Map<String, String>> getYhteystietoArvos() {
        return _yhteystietoArvos;
    }

    public void setYhteystietoArvos(List<Map<String, String>> yhteystietoArvos) {
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

    public List<Map<String, String>> getYhteystiedot() {
        if (_yhteystiedot == null) {
            _yhteystiedot = new ArrayList<Map<String, String>>();
        }
        return _yhteystiedot;
    }

    public void setYhteystiedot(List<Map<String, String>> _yhteystiedot) {
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
