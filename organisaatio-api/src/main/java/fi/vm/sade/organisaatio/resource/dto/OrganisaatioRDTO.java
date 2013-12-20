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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API used DTO, ie. "RDTO" for transmitting Organisaatio related data over REST.
 *
 * .. well, actually "OrganisaatioDTO" and "Organisaatio" were already used and I wanted to avoid confusion. :)
 *
 * "Natural key":
 * <ul>
 * <li>Koulutustoimija - y-tunnus</li>
 * <li>Oppilaitos - oppilaitosnumero</li>
 * <li>Toimipiste - oppilaitosnro + toimipisteenjärjestysnumero (konkatenoituna) sekä yhkoulukoodi</li>
 * </ul>
 *
 * @author mlyly
 */
public class OrganisaatioRDTO implements Serializable {

    private String _oid;
    private int _version;
    private Date _alkuPvm;
    private Date _lakkautusPvm;
    private Date _ytjPaivitysPvm;
    private List<String> _kieletUris;
    private List<String> _tyypit;
    private List<String> _vuosiluokat;
    private Map<String, String> _nimi;
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
    private String _kuvaus;
    private Map<String, String> _kuvaus2;
    private String _parentOid;
    private String _parentOidPath;
    private OrganisaatioMetaDataRDTO _metadata;
    private String yhteishaunKoulukoodi;

    private List<Map<String, String>> _yhteystietoArvos = null;


    public String getOid() {
        return _oid;
    }

    public void setOid(String _oid) {
        this._oid = _oid;
    }

    public int getVersion() {
        return _version;
    }

    public void setVersion(int _version) {
        this._version = _version;
    }

    public Date getAlkuPvm() {
        return _alkuPvm;
    }

    public void setAlkuPvm(Date _alkuPvm) {
        this._alkuPvm = _alkuPvm;
    }

    public Date getLakkautusPvm() {
        return _lakkautusPvm;
    }

    public void setLakkautusPvm(Date _lakkautusPvm) {
        this._lakkautusPvm = _lakkautusPvm;
    }

    public Date getYTJPaivitysPvm() {
        return _ytjPaivitysPvm;
    }

    public void setYTJPaivitysPvm(Date _ytjPaivitysPvm) {
        this._ytjPaivitysPvm = _ytjPaivitysPvm;
    }

    public List<String> getKieletUris() {
        if (_kieletUris == null) {
            _kieletUris = new ArrayList<String>();
        }
        return _kieletUris;
    }

    public void setKieletUris(List<String> _kieletUris) {
        this._kieletUris = _kieletUris;
    }

    public String getMaaUri() {
        return _maaUri;
    }

    public void setMaaUri(String _maaUri) {
        this._maaUri = _maaUri;
    }

    public String getDomainNimi() {
        return _domainNimi;
    }

    public void setDomainNimi(String _domainNimi) {
        this._domainNimi = _domainNimi;
    }

    public String getKotipaikkaUri() {
        return _kotipaikkaUri;
    }

    public void setKotipaikkaUri(String _kotipaikkaUri) {
        this._kotipaikkaUri = _kotipaikkaUri;
    }

    public Map<String, String> getNimi() {
        if (_nimi == null) {
            _nimi = new HashMap<String, String>();
        }
        return _nimi;
    }

    public void setNimi(Map<String, String> _nimi) {
        this._nimi = _nimi;
    }

    public String getOppilaitosKoodi() {
        return _oppilaitosKoodi;
    }

    public void setOppilaitosKoodi(String _oppilaitosKoodi) {
        this._oppilaitosKoodi = _oppilaitosKoodi;
    }

    public String getOppilaitosTyyppiUri() {
        return _oppilaitosTyyppiUri;
    }

    public void setOppilaitosTyyppiUri(String _oppilaitosTyyppiUri) {
        this._oppilaitosTyyppiUri = _oppilaitosTyyppiUri;
    }

    public String getYTunnus() {
        return _yTunnus;
    }

    public void setYTunnus(String _yTunnus) {
        this._yTunnus = _yTunnus;
    }

    public List<String> getTyypit() {
        if (_tyypit == null) {
            _tyypit = new ArrayList<String>();
        }
        return _tyypit;
    }

    public void setTyypit(List<String> _tyypit) {
        this._tyypit = _tyypit;
    }

    public String getToimipistekoodi() {
        return _toimipistekoodi;
    }

    public void setToimipistekoodi(String _toimipistekoodi) {
        this._toimipistekoodi = _toimipistekoodi;
    }

    public String getYritysmuoto() {
        return _yritysmuoto;
    }

    public void setYritysmuoto(String _yritysmuoto) {
        this._yritysmuoto = _yritysmuoto;
    }

    public List<String> getVuosiluokat() {
        if (_vuosiluokat == null) {
            _vuosiluokat = new ArrayList<String>();
        }
        return _vuosiluokat;
    }

    public void setVuosiluokat(List<String> _vuosiluokat) {
        this._vuosiluokat = _vuosiluokat;
    }

    public Map<String, String> getKayntiosoite() {
        if (_kayntiosoite == null) {
            _kayntiosoite = new HashMap<String, String>();
        }
        return _kayntiosoite;
    }

    public void setKayntiosoite(Map<String, String> _kayntiosoite) {
        this._kayntiosoite = _kayntiosoite;
    }

    public Map<String, String> getPostiosoite() {
        if (_postiosoite == null) {
            _postiosoite = new HashMap<String, String>();
        }
        return _postiosoite;
    }

    public void setPostiosoite(Map<String, String> _postiosoite) {
        this._postiosoite = _postiosoite;
    }

    public String getKuvaus() {
        return _kuvaus;
    }

    public void setKuvaus(String _kuvaus) {
        this._kuvaus = _kuvaus;
    }

    public Map<String, String> getKuvaus2() {
        if (_kuvaus2 == null) {
            _kuvaus2 = new HashMap<String, String>();
        }
        return _kuvaus2;
    }

    public void setKuvaus2(Map<String, String> _kuvaus2) {
        this._kuvaus2 = _kuvaus2;
    }

    public String getParentOid() {
        return _parentOid;
    }

    public void setParentOid(String _parentOid) {
        this._parentOid = _parentOid;
    }

    public String getParentOidPath() {
        return _parentOidPath;
    }

    public void setParentOidPath(String _parentOidPath) {
        this._parentOidPath = _parentOidPath;
    }

    public OrganisaatioMetaDataRDTO getMetadata() {
        return _metadata;
    }

    public void setMetadata(OrganisaatioMetaDataRDTO _metadata) {
        this._metadata = _metadata;
    }

    public String getEmailOsoite() {
        return _emailOsoite;
    }

    public void setEmailOsoite(String _emailOsoite) {
        this._emailOsoite = _emailOsoite;
    }

    public String getFaksinumero() {
        return _faksinumero;
    }

    public void setFaksinumero(String _faksinumero) {
        this._faksinumero = _faksinumero;
    }

    public String getPuhelinnumero() {
        return _puhelinnumero;
    }

    public void setPuhelinnumero(String _puhelinnumero) {
        this._puhelinnumero = _puhelinnumero;
    }

    public String getWwwOsoite() {
        return _wwwOsoite;
    }

    public void setWwwOsoite(String _wwwOsoite) {
        this._wwwOsoite = _wwwOsoite;
    }

    public String getYhteishaunKoulukoodi() {
        return yhteishaunKoulukoodi;
    }

    public void setYhteishaunKoulukoodi(String yhteishaunKoulukoodi) {
        this.yhteishaunKoulukoodi = yhteishaunKoulukoodi;
    }

    public List<Map<String, String>> getYhteystietoArvos() {
        return _yhteystietoArvos;
    }

    public void setYhteystietoArvos(List<Map<String, String>> yhteystietoArvos) {
        this._yhteystietoArvos = yhteystietoArvos;
    }

}
