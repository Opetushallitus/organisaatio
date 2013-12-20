/*
 *
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
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
 * European Union Public Licence for more details.
 */
package fi.vm.sade.organisaatio.ui.model;

import org.apache.commons.lang.builder.ToStringBuilder;

import fi.vm.sade.generic.ui.component.MultiLingualTextImpl;
import fi.vm.sade.organisaatio.api.model.types.EmailDTO;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.model.types.OsoiteDTO;
import fi.vm.sade.organisaatio.api.model.types.OsoiteTyyppi;
import fi.vm.sade.organisaatio.api.model.types.PuhelinNumeroTyyppi;
import fi.vm.sade.organisaatio.api.model.types.PuhelinnumeroDTO;
import fi.vm.sade.organisaatio.api.model.types.WwwDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoArvoDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoDTO;
import fi.vm.sade.organisaatio.helper.OrganisaatioDisplayHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * For ui editing of child Organisaatio data and as dto for saving the data.
 * 
* @author Antti Salonen
 */
public class OrganisaatioModel implements Serializable {
    
	private static final long serialVersionUID = 1L;
	
	private OrganisaatioDTO organisaatio = new OrganisaatioDTO();
    private OsoiteDTO postiosoite = new OsoiteDTO();
    private OsoiteDTO kayntiosoite = new OsoiteDTO();
    private OsoiteDTO ruotsiKayntiOsoite = new OsoiteDTO();
    private OsoiteDTO ruotsiPostiOsoite = new OsoiteDTO();
    private PuhelinnumeroDTO puhelin = new PuhelinnumeroDTO();
    private PuhelinnumeroDTO faksi = new PuhelinnumeroDTO();
    private EmailDTO email = new EmailDTO();
    private WwwDTO www = new WwwDTO();
    private List<OsoiteDTO> muutOsoitteet = new ArrayList<OsoiteDTO>();
    private List<YhteystietoDTO> muutYhteystiedot = new ArrayList<YhteystietoDTO>();
    private List<YhteystietoArvoDTO> yhteystietoArvos = new ArrayList<YhteystietoArvoDTO>();
    private String oid;
    private String mlNimiFi;
    private Date alkuPvm;
    private Set<String> organisaatiotyypit;
    private Set<String> organisaatioKielet;
    private Set<String> vuosiluokat;
    private String kotipaikka;
    private OrganisaatioKuvailevatTiedotModel kuvailevatTiedot = new OrganisaatioKuvailevatTiedotModel();
    private String ytunnus;
    private String virastoTunnus;
   
    public String getYtunnus() {
		return ytunnus;
	}
    
    public void setYtunnus(String ytunnus) {
		this.ytunnus = ytunnus;
	}
    
    public String getVirastoTunnus() {
		return virastoTunnus;
	}
    
    public void setVirastoTunnus(String virastoTunnus) {
		this.virastoTunnus = virastoTunnus;
	}
    
    public Date getAlkuPvm() {
        return alkuPvm;
    }

    public void setAlkuPvm(Date alkuPvm) {
        this.alkuPvm = alkuPvm;
    }

    public Date getLakkautusPvm() {
        return lakkautusPvm;
    }

    public void setLakkautusPvm(Date lakkautusPvm) {
        this.lakkautusPvm = lakkautusPvm;
    }
    private Date lakkautusPvm;

    /**
     * Get Finnish organisation name.
     *
     * @return the organisation name
     */
    public String getMlNimiFi() {
        return mlNimiFi;
    }

    /**
     * Set Finnish organisation name.
     *
     * @param nimiFi
     */
    public void setMlNimiFi(String nimiFi) {
        this.mlNimiFi = nimiFi;
    }

    /**
     * Get Swedish organisation name.
     *
     * @return the organisation name
     */
    public String getMlNimiSv() {
        return mlNimiSv;
    }

    /**
     * Set Swedish organisation name.
     *
     * @param nimiSv
     */
    public void setMlNimiSv(String nimiSv) {
        this.mlNimiSv = nimiSv;
    }

    /**
     * Get English organisation name.
     *
     * @return the organisation name
     */
    public String getMlNimiEn() {
        return mlNimiEn;
    }

    /**
     * Set English organisation name.
     *
     * @param nimiEn
     */
    public void setMlNimiEn(String nimiEn) {
        this.mlNimiEn = nimiEn;
    }
    private String mlNimiSv;
    private String mlNimiEn;
    private transient MultiLingualTextImpl mlNimi = new MultiLingualTextImpl(this, "mlNimi");

    /**
     *
     * @return
     */
    public MultiLingualTextImpl getMlNimi() {
        return mlNimi;
    }

    /**
     *
     * @param mlNimi
     */
    public void setMlNimi(MultiLingualTextImpl mlNimi) {
        this.mlNimi = mlNimi;
    }

    public OrganisaatioModel() {
        setYhteystietotyyppis();
    }

    public OrganisaatioModel(OrganisaatioDTO organisaatio) {
        setYhteystietotyyppis();
        this.organisaatio = organisaatio;
        this.postiosoite = valueOrDefault(OrganisaatioDisplayHelper.getOsoiteByType(organisaatio, OsoiteTyyppi.POSTI), this.postiosoite);
        this.kayntiosoite = valueOrDefault(OrganisaatioDisplayHelper.getOsoiteByType(organisaatio, OsoiteTyyppi.KAYNTI), this.kayntiosoite);
        this.ruotsiKayntiOsoite = valueOrDefault(OrganisaatioDisplayHelper.getOsoiteByType(organisaatio, OsoiteTyyppi.RUOTSI_KAYNTI), this.ruotsiKayntiOsoite);
        this.ruotsiPostiOsoite = valueOrDefault(OrganisaatioDisplayHelper.getOsoiteByType(organisaatio, OsoiteTyyppi.RUOTSI_POSTI), this.ruotsiPostiOsoite);
        this.puhelin = valueOrDefault(OrganisaatioDisplayHelper.getPuhelinNumeroByType(organisaatio.getYhteystiedot(), PuhelinNumeroTyyppi.PUHELIN), this.puhelin);
        this.faksi = valueOrDefault(OrganisaatioDisplayHelper.getPuhelinNumeroByType(organisaatio.getYhteystiedot(), PuhelinNumeroTyyppi.FAKSI), this.faksi);
        this.email = (OrganisaatioDisplayHelper.getOrganisaatioEmail(organisaatio) != null) ? OrganisaatioDisplayHelper.getOrganisaatioEmail(organisaatio) : this.email;
        this.www = (OrganisaatioDisplayHelper.getOrganisaatioWww(organisaatio) != null) ? OrganisaatioDisplayHelper.getOrganisaatioWww(organisaatio) : this.www;
        this.muutYhteystiedot = valueOrDefault(OrganisaatioDisplayHelper.getMuutYhteystiedot(organisaatio), new ArrayList<YhteystietoDTO>());
        this.muutOsoitteet = organisaatio.getMuutOsoitteet();
        this.yhteystietoArvos = organisaatio.getYhteystietoArvos();
        this.mlNimiFi = getNimiValue(organisaatio, "fi");
        this.mlNimiSv = getNimiValue(organisaatio, "sv");
        this.mlNimiEn = getNimiValue(organisaatio, "en");
        this.alkuPvm = organisaatio.getAlkuPvm();
        this.lakkautusPvm = organisaatio.getLakkautusPvm();
        this.oid = organisaatio.getOid();
        this.kuvailevatTiedot = new OrganisaatioKuvailevatTiedotModel(organisaatio.getKuvailevatTiedot());
        this.kotipaikka = organisaatio.getKotipaikka();
        this.ytunnus = organisaatio.getYtunnus();
        this.virastoTunnus = organisaatio.getVirastoTunnus();
    }
    
    private String getNimiValue(OrganisaatioDTO org, String lang) {
    	if (org.getNimi() == null) {
    		return null;
    	}
    	for (Teksti curTeksti :org.getNimi().getTeksti()) {
    		if (curTeksti.getKieliKoodi().equals(lang) 
    				&& curTeksti.getValue() != null 
    				&& !curTeksti.getValue().isEmpty()) {
    			return curTeksti.getValue();
    		}
    	}
    	return null;			
    }
    
    

    /**
     * @return the nimi
     */
    public String getNimi() {
        return mlNimi.getClosest(Locale.getDefault());
    }

    private void setYhteystietotyyppis() {
        postiosoite.setOsoiteTyyppi(OsoiteTyyppi.POSTI);
        kayntiosoite.setOsoiteTyyppi(OsoiteTyyppi.KAYNTI);
        if (puhelin != null) {
            puhelin.setTyyppi(PuhelinNumeroTyyppi.PUHELIN);
        } 
        if (faksi != null) {
            faksi.setTyyppi(PuhelinNumeroTyyppi.FAKSI);
        }
        ruotsiKayntiOsoite.setOsoiteTyyppi(OsoiteTyyppi.RUOTSI_KAYNTI);
        ruotsiPostiOsoite.setOsoiteTyyppi(OsoiteTyyppi.RUOTSI_POSTI);
    }

    public void convertToOrganisaatio() {

    	//this.organisaatio.sety
        organisaatio.getYhteystiedot().clear();
        organisaatio.getYhteystiedot().add(postiosoite);
        organisaatio.getYhteystiedot().add(kayntiosoite);
        organisaatio.getYhteystiedot().add(ruotsiKayntiOsoite);
        organisaatio.getYhteystiedot().add(ruotsiPostiOsoite);
        organisaatio.getYhteystiedot().add(puhelin);
        if (faksi.getPuhelinnumero() != null && faksi.getPuhelinnumero().length() > 0) {
            organisaatio.getYhteystiedot().add(faksi);
        }
        organisaatio.getYhteystiedot().add(email);
        if (www.getWwwOsoite() != null && !www.getWwwOsoite().isEmpty()) {
        	organisaatio.getYhteystiedot().add(www);
        }
        organisaatio.getYhteystiedot().addAll(muutYhteystiedot);
        // Following removeAll() + addAll() makes union of muutYhteystiedot + muutOsoitteet
        organisaatio.getYhteystiedot().removeAll(muutOsoitteet);
        organisaatio.getYhteystiedot().addAll(muutOsoitteet);

        convertNimiToOrganisaatio();

        organisaatio.setYtunnus(ytunnus);
        organisaatio.setVirastoTunnus(virastoTunnus);
        organisaatio.setAlkuPvm(alkuPvm);
        organisaatio.setLakkautusPvm(lakkautusPvm);
        organisaatio.setKuvailevatTiedot(kuvailevatTiedot.convertToDto());
        organisaatio.setKotipaikka(kotipaikka);

    }
    
    private void convertNimiToOrganisaatio() {
    	MonikielinenTekstiTyyppi nimi = new MonikielinenTekstiTyyppi();
    	if (mlNimiFi != null && ! mlNimiFi.isEmpty()) {
    		Teksti nimifi = new Teksti();
    		nimifi.setKieliKoodi("fi");
    		nimifi.setValue(mlNimiFi);
    		nimi.getTeksti().add(nimifi);
    	}
    	
    	if (mlNimiSv != null && !mlNimiSv.isEmpty()) {
    		Teksti nimisv = new Teksti();
    		nimisv.setKieliKoodi("sv");
    		nimisv.setValue(this.mlNimiSv);
    		nimi.getTeksti().add(nimisv);
    	}
    	
    	if (mlNimiEn != null && !mlNimiEn.isEmpty()) {
    		Teksti nimien = new Teksti();
    		nimien.setKieliKoodi("en");
    		nimien.setValue(this.mlNimiEn);
    		nimi.getTeksti().add(nimien);
    	}
    	organisaatio.setNimi(nimi);
    }

    public List<YhteystietoDTO> getMuutYhteystiedot() {
        return muutYhteystiedot;
    }

    public void setMuutYhteystiedot(List<YhteystietoDTO> muutYhteystiedot) {
        this.muutYhteystiedot = muutYhteystiedot;
    }

    public String getParentOid() {
        return organisaatio.getParentOid();
    }

    public void setParentOid(String parentOid) {
        organisaatio.setParentOid(parentOid);
    }

    public List<YhteystietoArvoDTO> getYhteystietoArvos() {
        return yhteystietoArvos;
    }

    public void setYhteystietoArvos(List<YhteystietoArvoDTO> yhteystietoArvos) {
        this.yhteystietoArvos = yhteystietoArvos;
    }

    private <T> T valueOrDefault(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    public OrganisaatioDTO getOrganisaatio() {
        return organisaatio;
    }

    public void setOrganisaatio(OrganisaatioDTO organisaatio) {
        this.organisaatio = organisaatio;
    }

    public OsoiteDTO getPostiosoite() {
        return postiosoite;
    }

    public void setPostiosoite(OsoiteDTO osoiteDTO) {
        postiosoite = osoiteDTO;
    }

    public OsoiteDTO getKayntiosoite() {
        return kayntiosoite;
    }

    public void setKayntiosoite(OsoiteDTO kayntiosoite) {
        this.kayntiosoite = kayntiosoite;
    }

    public PuhelinnumeroDTO getPuhelin() {
        return puhelin;
    }

    public void setPuhelin(PuhelinnumeroDTO puhelin) {
        this.puhelin = puhelin;
    }

    public PuhelinnumeroDTO getFaksi() {
        return faksi;
    }

    public void setFaksi(PuhelinnumeroDTO faksi) {
        this.faksi = faksi;
    }

    public WwwDTO getWww() {
        return www;
    }

    public void setWww(WwwDTO www) {
        this.www = www;
    }

    public EmailDTO getEmail() {
        return email;
    }

    public void setEmail(EmailDTO email) {
        this.email = email;
    }

    private boolean isEmptyYhteystieto(YhteystietoDTO yhteystieto) {
        if (yhteystieto instanceof PuhelinnumeroDTO) {
            return ((PuhelinnumeroDTO) yhteystieto).getPuhelinnumero() == null;
        }
        if (yhteystieto instanceof OsoiteDTO) {
            return ((OsoiteDTO) yhteystieto).getOsoite() == null;
        }
        if (yhteystieto instanceof WwwDTO) {
            return ((WwwDTO) yhteystieto).getWwwOsoite() == null;
        }
        if (yhteystieto instanceof EmailDTO) {
            return ((EmailDTO) yhteystieto).getEmail() == null;
        }
        return false;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * must be cloned before sending to service as dto
     *
     * @param replaceEmptiesWithNulls if true, replace empty properties with
     * nulls, so validation will work correctly if eg. faksi -field was not
     * inputted
     */
    // TODO: eroon tästä kloonaushässäkästä
    public OrganisaatioModel clone(boolean replaceEmptiesWithNulls) {
        OrganisaatioModel result = null;
        try {
            result = this.getClass().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        result.organisaatio = organisaatio;
        result.postiosoite = replaceEmptyWithNull(postiosoite, replaceEmptiesWithNulls);
        result.kayntiosoite = replaceEmptyWithNull(kayntiosoite, replaceEmptiesWithNulls);
        result.puhelin = replaceEmptyWithNull(puhelin, replaceEmptiesWithNulls);
        result.faksi = replaceEmptyWithNull(faksi, replaceEmptiesWithNulls);
        result.www = replaceEmptyWithNull(www, replaceEmptiesWithNulls);
        result.email = replaceEmptyWithNull(email, replaceEmptiesWithNulls);

        result.muutYhteystiedot = muutYhteystiedot;
        result.muutOsoitteet = muutOsoitteet;
        result.yhteystietoArvos = yhteystietoArvos;

        return result;
    }

    private <T extends YhteystietoDTO> T replaceEmptyWithNull(T yhteystieto, boolean replaceEmptiesWithNulls) {
        if (yhteystieto == null || replaceEmptiesWithNulls && isEmptyYhteystieto(yhteystieto)) {
            return null;
        } else {
            return yhteystieto;
        }
    }

    /**
     * @return the muutOsoitteet
     */
    public List<OsoiteDTO> getMuutOsoitteet() {
        return muutOsoitteet;
    }

    /**
     * @param muutOsoitteet the muutOsoitteet to set
     */
    public void setMuutOsoitteet(List<OsoiteDTO> muutOsoitteet) {
        this.muutOsoitteet = muutOsoitteet;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }
    
    /**
     * Gets the organisaatioKielet
     * @return the organisaatioKielet to get
     */
    public Set<String> getOrganisaatioKielet() {
    	organisaatioKielet = new HashSet<String>();
    	for (String curKieli : organisaatio.getKielet()) {
    		organisaatioKielet.add(curKieli);
    	}
    	return organisaatioKielet;
    }
    
    /**
     * Sets the organisaatioKielet.
     * @param organisaatioKielet the organisaatioKielet to set
     */
    public void setOrganisaatioKielet(Set<String> organisaatioKielet) {
    	organisaatio.getKielet().clear();
    	organisaatio.getKielet().addAll(organisaatioKielet);
    	this.organisaatioKielet = organisaatioKielet;
    }

    public String getPuhelinnumero() {
        return this.puhelin.getPuhelinnumero();
    }

    public void setPuhelinnumero(String puhelinnumero) {
        this.puhelin.setPuhelinnumero(puhelinnumero);
    }

    
    public String getFaksinumero() {
        return this.faksi.getPuhelinnumero();
    }

    public void setFaksinumero(String faksinumero) {
        this.faksi.setPuhelinnumero(faksinumero);
    }

    public String getWwwOsoite() {
        return this.www.getWwwOsoite();
    }

    public void setWwwOsoite(String wwwOsoite) {
        this.www.setWwwOsoite(wwwOsoite);
    }

    public String getEmailOsoite() {
        return this.email.getEmail();
    }
    
    public void setEmailOsoite(String emailOsoite) {
        this.email.setEmail(emailOsoite);
    }

    public String getOppilaitosKoodi() {
        return this.organisaatio.getOppilaitosKoodi();
    }

    public void setOppilaitosKoodi(String oppilaitosKoodi) {
        this.organisaatio.setOppilaitosKoodi(oppilaitosKoodi);
    }
    
    /**
     * Getting organisaatiotyypit. Getting values from OrganisaatioDTO. 
     * @return
     */
    public Set<String> getOrganisaatiotyypit() {
        organisaatiotyypit = new HashSet<String>();
        for (OrganisaatioTyyppi curTyyppi: organisaatio.getTyypit()) {
            organisaatiotyypit.add(curTyyppi.value());
        }
        return organisaatiotyypit;
    }

    /**
     * Setting organisaatiotyypit. Converting values to OrganisaatioDTO.
     * @param organisaatiotyypit - the organisaatiotyypit to set.
     */
    public void setOrganisaatiotyypit(Set<String> organisaatiotyypit) {
        organisaatio.getTyypit().clear();
        for (String curT : organisaatiotyypit) {
            organisaatio.getTyypit().add(OrganisaatioTyyppi.fromValue(curT));
        }
        this.organisaatiotyypit = organisaatiotyypit;
    }

    public void setOppilaitostyyppi(String oppilaitostyyppi) {
        organisaatio.setOppilaitosTyyppi(oppilaitostyyppi);
    }

    public String getOppilaitostyyppi() {
        return organisaatio.getOppilaitosTyyppi();
    }

    public OrganisaatioKuvailevatTiedotModel getKuvailevatTiedot() {
        return kuvailevatTiedot;
    }

    public void setKuvailevatTiedot(OrganisaatioKuvailevatTiedotModel kuvailevatTiedot) {
        this.kuvailevatTiedot = kuvailevatTiedot;
    }

    public String getKotipaikka() {
        return kotipaikka;
    }

    public void setKotipaikka(String kotipaikka) {
        this.kotipaikka = kotipaikka;
    }

    public Set<String> getVuosiluokat() {
        vuosiluokat = new HashSet<String>();
        for (String curVuosiluokka : organisaatio.getVuosiluokat()) {
            vuosiluokat.add(curVuosiluokka);
        }
        return vuosiluokat;
    }

    public void setVuosiluokat(Set<String> vuosiluokat) {
        organisaatio.getVuosiluokat().clear();
        organisaatio.getVuosiluokat().addAll(vuosiluokat);
        this.vuosiluokat = vuosiluokat;
    }

    public OsoiteDTO getRuotsiKayntiOsoite() {
        return ruotsiKayntiOsoite;
    }

    public void setRuotsiKayntiOsoite(OsoiteDTO ruotsiKayntiOsoite) {
        this.ruotsiKayntiOsoite = ruotsiKayntiOsoite;
    }

    public OsoiteDTO getRuotsiPostiOsoite() {
        return ruotsiPostiOsoite;
    }

    public void setRuotsiPostiOsoite(OsoiteDTO ruotsiPostiOsoite) {
        this.ruotsiPostiOsoite = ruotsiPostiOsoite;
    }
}
