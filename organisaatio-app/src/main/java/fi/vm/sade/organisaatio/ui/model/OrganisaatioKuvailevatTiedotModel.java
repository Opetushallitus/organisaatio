
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.ui.component.MultiLingualTextImpl;
import fi.vm.sade.organisaatio.api.model.types.EmailDTO;
import fi.vm.sade.organisaatio.api.model.types.HakutoimistoTyyppi;
import fi.vm.sade.organisaatio.api.model.types.KuvailevaTietoTyyppi;
import fi.vm.sade.organisaatio.api.model.types.KuvailevaTietoTyyppiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioKuvailevatTiedotTyyppi;
import fi.vm.sade.organisaatio.api.model.types.OsoiteDTO;
import fi.vm.sade.organisaatio.api.model.types.OsoiteTyyppi;
import fi.vm.sade.organisaatio.api.model.types.PuhelinNumeroTyyppi;
import fi.vm.sade.organisaatio.api.model.types.PuhelinnumeroDTO;
import fi.vm.sade.organisaatio.api.model.types.SoMeLinkkiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.SoMeLinkkiTyyppiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.WwwDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteyshenkiloTyyppi;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoDTO;

/**
 * 
 * @author Markus
 *
 */
public class OrganisaatioKuvailevatTiedotModel {
    
    private Map<String,LOPTiedotModel> lopTiedot = new HashMap<String,LOPTiedotModel>();
    
    private String facebook;
    private String twitter;
    private String linkedin;
    private String googlePlus;
    private String muu1;
    private String muu2;
    
    private OrganisaatioKuvaModel kuva;
    
    private OsoiteDTO postiosoite;
    private OsoiteDTO kayntiosoite;
    private OsoiteDTO ruotsiPostiOsoite;
    private OsoiteDTO ruotsiKayntiOsoite;
    private OsoiteDTO englantiPostiOsoite;
    private OsoiteDTO englantiKayntiOsoite;    
    private PuhelinnumeroDTO puhelin;
    private PuhelinnumeroDTO faksi;
    private EmailDTO email;
    private WwwDTO wwwOsoite;
    private String otNimiFi;
    private String otNimiSv;
    private String otNimiEn;
    private transient MultiLingualTextImpl otNimi = new MultiLingualTextImpl(this, "otNimi");
    private List<KielikaannosModel> additionalNimet = new ArrayList<KielikaannosModel>();
    private boolean yksiNimi = false;
    

    private YhteyshenkiloModel ectsYhteyshenkilo;


    private OrganisaatioKuvailevatTiedotTyyppi tiedotDto = new OrganisaatioKuvailevatTiedotTyyppi();
    
    public OrganisaatioKuvailevatTiedotModel() {
        
    }
    
    OrganisaatioKuvailevatTiedotModel(OrganisaatioKuvailevatTiedotTyyppi tiedot) {
        this.tiedotDto = tiedot != null ? tiedot : new OrganisaatioKuvailevatTiedotTyyppi();
        createLopMap();
        createSoMeLinks();
        
        kuva = new OrganisaatioKuvaModel(tiedotDto.getKuva());
        
        if (tiedotDto.getHakutoimisto() != null) {
        	for (YhteystietoDTO ytd : tiedotDto.getHakutoimisto().getOpintotoimistoYhteystiedot()) {
        		if (ytd instanceof WwwDTO) {
        			wwwOsoite = (WwwDTO) ytd;
        			break;
        		}
        	}
        }

        postiosoite  = getOsoiteByTyyppi(OsoiteTyyppi.POSTI);
        kayntiosoite = getOsoiteByTyyppi(OsoiteTyyppi.KAYNTI);
        ruotsiPostiOsoite  = getOsoiteByTyyppi(OsoiteTyyppi.RUOTSI_POSTI);
        ruotsiKayntiOsoite = getOsoiteByTyyppi(OsoiteTyyppi.RUOTSI_KAYNTI);
        englantiPostiOsoite  = getOsoiteByTyyppi(OsoiteTyyppi.ULKOMAINEN_POSTI);
        englantiKayntiOsoite = getOsoiteByTyyppi(OsoiteTyyppi.ULKOMAINEN_KAYNTI);
        
        puhelin = getPuhelinnumeroByTyyppi(PuhelinNumeroTyyppi.PUHELIN);
        faksi = getPuhelinnumeroByTyyppi(PuhelinNumeroTyyppi.FAKSI);
        email = getEmailFromYts();
        createOtNimi();
        ectsYhteyshenkilo = new YhteyshenkiloModel(tiedotDto.getHakutoimisto() != null 
                ? tiedotDto.getHakutoimisto().getEctsYhteyshenkilo() : null); 
    }
    
    private void createOtNimi() {
        this.otNimiFi = getNimiValue(I18N.getMessage("OrganisaatioKuvailevatTiedotFormView.fiUri"));
        this.otNimiSv = getNimiValue(I18N.getMessage("OrganisaatioKuvailevatTiedotFormView.svUri"));
        this.otNimiEn = getNimiValue(I18N.getMessage("OrganisaatioKuvailevatTiedotFormView.enUri"));
        
        if (tiedotDto.getHakutoimisto() == null || tiedotDto.getHakutoimisto().getOpintotoimistoNimi() == null) {
            return;
        }
        for (Teksti curTeksti : tiedotDto.getHakutoimisto().getOpintotoimistoNimi().getTeksti()) {
            if (!curTeksti.getKieliKoodi().equals(I18N.getMessage("OrganisaatioKuvailevatTiedotFormView.fiUri")) 
                    && !curTeksti.getKieliKoodi().equals(I18N.getMessage("OrganisaatioKuvailevatTiedotFormView.svUri")) 
                    && !curTeksti.getKieliKoodi().equals(I18N.getMessage("OrganisaatioKuvailevatTiedotFormView.enUri"))) {
                this.additionalNimet.add(new KielikaannosModel(curTeksti.getKieliKoodi(), curTeksti.getValue()));
            }
        }
        
        handleSamaNimi();
    }
    
    private void handleSamaNimi() {
        if (this.otNimiFi == null || this.otNimiEn == null || this.otNimiSv == null) {
            return;
        }
        
        if (this.otNimiFi.equals(this.otNimiSv) && this.otNimiSv.equals(this.otNimiEn)) {
            this.yksiNimi = true;
        }
    }
    
    private String getNimiValue(String lang) {
        if (tiedotDto.getHakutoimisto() == null || tiedotDto.getHakutoimisto().getOpintotoimistoNimi() == null) {
            return null;
        }
        for (Teksti curTeksti : tiedotDto.getHakutoimisto().getOpintotoimistoNimi().getTeksti()) {
            if (curTeksti.getKieliKoodi().equals(lang)) {
                return curTeksti.getValue();
            }
        }
        return null;
    }
    
    private void createSoMeLinks() {
        facebook = fetchSoMeLink(SoMeLinkkiTyyppiTyyppi.FACEBOOK);
        twitter = fetchSoMeLink(SoMeLinkkiTyyppiTyyppi.TWITTER);
        linkedin = fetchSoMeLink(SoMeLinkkiTyyppiTyyppi.LINKED_IN);
        googlePlus = fetchSoMeLink(SoMeLinkkiTyyppiTyyppi.GOOGLE_PLUS);
        fetchMuuSoMeLinks();
    }
    
    private void fetchMuuSoMeLinks() {
        List<String> muutLinkit = new ArrayList<String>();
        for (SoMeLinkkiTyyppi curLink : tiedotDto.getSoMeLinkit()) {
            if (curLink.getTyyppi().value().equals(SoMeLinkkiTyyppiTyyppi.MUU.value())) {
                muutLinkit.add(curLink.getSisalto());
            }
        }
        if (!muutLinkit.isEmpty()) {
            this.muu1 = muutLinkit.get(0);
        }
        if (muutLinkit.size() > 1) {
            this.muu2 = muutLinkit.get(1);
        }
    }
    
    
    private String fetchSoMeLink(SoMeLinkkiTyyppiTyyppi tyyppi) {
        String linkki = null;
        for (SoMeLinkkiTyyppi curLink : tiedotDto.getSoMeLinkit()) {
            if (curLink.getTyyppi().value().equals(tyyppi.value())) {
                return curLink.getSisalto();
            }
        }
        return linkki;
    }
    
    private void createLopMap() {
        for (KuvailevaTietoTyyppi curTieto : this.tiedotDto.getVapaatKuvaukset()) {
                createKuvausForLanguages(curTieto);
        }
    }
    
    private void createKuvausForLanguages(KuvailevaTietoTyyppi kuvailevaTieto) {
        if (kuvailevaTieto == null || kuvailevaTieto.getSisalto() == null) {
            return;
        }
        KuvailevaTietoTyyppiTyyppi tyyppi = kuvailevaTieto.getTyyppi();
        for (Teksti curTeksti : kuvailevaTieto.getSisalto().getTeksti()) {
            String kielikoodi = curTeksti.getKieliKoodi();
            LOPTietoModel tieto = new LOPTietoModel(tyyppi, curTeksti.getValue());
            if (lopTiedot.containsKey(kielikoodi)) {
                lopTiedot.get(kielikoodi).getTiedot().add(tieto);
            } else {
                List<LOPTietoModel> lopList = new ArrayList<LOPTietoModel>();
                lopList.add(tieto);
                LOPTiedotModel tiedot = new LOPTiedotModel();
                tiedot.setTiedot(lopList);
                lopTiedot.put(kielikoodi, tiedot);
            }
        }
    }
    
    
    
    
    OrganisaatioKuvailevatTiedotTyyppi convertToDto() {
        convertVapaatKuvaukset();
        convertSoMeLinkit();
        tiedotDto.setKuva(kuva.convertToDto());
        convertOpintotoimistoNimi();
        convertYhteystiedot();
        YhteyshenkiloTyyppi ytTyyppi = ectsYhteyshenkilo.convertToDto();
        if (ytTyyppi != null && tiedotDto.getHakutoimisto() == null ) {
        	tiedotDto.setHakutoimisto(new HakutoimistoTyyppi());
        }
        if (tiedotDto.getHakutoimisto() != null) {
        	tiedotDto.getHakutoimisto().setEctsYhteyshenkilo(ytTyyppi);
        }
        return tiedotDto;
    }

    private void convertYhteystiedot() {
        HakutoimistoTyyppi toimisto;
        if (tiedotDto.getHakutoimisto() == null) {
            toimisto = new HakutoimistoTyyppi();
            tiedotDto.setHakutoimisto(toimisto);
        } else {
            toimisto = tiedotDto.getHakutoimisto();
        }
        
        toimisto.getOpintotoimistoYhteystiedot().clear();
        if (postiosoite!=null && !Strings.isNullOrEmpty(postiosoite.getOsoite())) {
        	toimisto.getOpintotoimistoYhteystiedot().add(postiosoite);
        }
        if (kayntiosoite!=null && !Strings.isNullOrEmpty(kayntiosoite.getOsoite())) {
        	toimisto.getOpintotoimistoYhteystiedot().add(kayntiosoite);
        }
        if (ruotsiPostiOsoite!=null && !Strings.isNullOrEmpty(ruotsiPostiOsoite.getOsoite())) {
        	toimisto.getOpintotoimistoYhteystiedot().add(ruotsiPostiOsoite);
        }
        if (ruotsiKayntiOsoite!=null && !Strings.isNullOrEmpty(ruotsiKayntiOsoite.getOsoite())) {
        	toimisto.getOpintotoimistoYhteystiedot().add(ruotsiKayntiOsoite);
        }
        if (englantiPostiOsoite!=null && !Strings.isNullOrEmpty(englantiPostiOsoite.getOsoite())) {
        	toimisto.getOpintotoimistoYhteystiedot().add(englantiPostiOsoite);
        }
        if (englantiKayntiOsoite!=null && !Strings.isNullOrEmpty(englantiKayntiOsoite.getOsoite())) {
        	toimisto.getOpintotoimistoYhteystiedot().add(englantiKayntiOsoite);
        }
        if (faksi!=null && !Strings.isNullOrEmpty(faksi.getPuhelinnumero())) {
        	toimisto.getOpintotoimistoYhteystiedot().add(faksi);
        }
        if (puhelin!=null && !Strings.isNullOrEmpty(puhelin.getPuhelinnumero())) {
        	toimisto.getOpintotoimistoYhteystiedot().add(puhelin);
        }
        if (email!=null && !Strings.isNullOrEmpty(email.getEmail())) {
        	toimisto.getOpintotoimistoYhteystiedot().add(email);
        }
        if (wwwOsoite!=null && !Strings.isNullOrEmpty(wwwOsoite.getWwwOsoite())) {
        	toimisto.getOpintotoimistoYhteystiedot().add(wwwOsoite);
        }
    }
    
    private void convertOpintotoimistoNimi() {
        if (otNimiFi != null || otNimiSv != null || otNimiEn != null || this.additionalNimet != null) {
            handleHakutoimistoNimiInit();
        }
        
        if (this.yksiNimi) {
            convertNimiValue(otNimiFi, I18N.getMessage("OrganisaatioKuvailevatTiedotFormView.fiUri"));
            convertNimiValue(otNimiFi, I18N.getMessage("OrganisaatioKuvailevatTiedotFormView.svUri"));
            convertNimiValue(otNimiFi, I18N.getMessage("OrganisaatioKuvailevatTiedotFormView.enUri"));
        } else {        
	        convertNimiValue(otNimiFi, I18N.getMessage("OrganisaatioKuvailevatTiedotFormView.fiUri"));
	        convertNimiValue(otNimiSv, I18N.getMessage("OrganisaatioKuvailevatTiedotFormView.svUri"));
	        convertNimiValue(otNimiEn, I18N.getMessage("OrganisaatioKuvailevatTiedotFormView.enUri"));
        }
        
        if (this.additionalNimet == null) {
            return;
        }
        
        for (KielikaannosModel curNimi : this.additionalNimet) {
            convertNimiValue(curNimi.getArvo(), curNimi.getKielikoodi());
        }
        
        
    }
    
    private void handleHakutoimistoNimiInit() {
        if (tiedotDto.getHakutoimisto() == null) {
            HakutoimistoTyyppi toimisto = new HakutoimistoTyyppi();
            tiedotDto.setHakutoimisto(toimisto);
        }
        if (tiedotDto.getHakutoimisto().getOpintotoimistoNimi() == null) {
            tiedotDto.getHakutoimisto().setOpintotoimistoNimi(new MonikielinenTekstiTyyppi());
        }
        tiedotDto.getHakutoimisto().getOpintotoimistoNimi().getTeksti().clear();
    }
    
    private void convertNimiValue(String nimiValue, String lang) {
        if (nimiValue != null && !nimiValue.isEmpty()) {
            Teksti nimi = new Teksti();
            nimi.setKieliKoodi(lang);
            nimi.setValue(nimiValue);
            tiedotDto.getHakutoimisto().getOpintotoimistoNimi().getTeksti().add(nimi);
        }
    }
    
    public Map<String, LOPTiedotModel> getLopTiedot() {
        return lopTiedot;
    }

    public void setLopTiedot(Map<String, LOPTiedotModel> lopTiedot) {
        this.lopTiedot = lopTiedot;
    }

    private void convertSoMeLinkit() {
        this.tiedotDto.getSoMeLinkit().clear();
        if (facebook != null && !facebook.isEmpty()) {
            addSomeLinkki(facebook, SoMeLinkkiTyyppiTyyppi.FACEBOOK);
        }
        if (twitter != null && !twitter.isEmpty()) {
            addSomeLinkki(twitter, SoMeLinkkiTyyppiTyyppi.TWITTER);
        }
        if (linkedin != null && !linkedin.isEmpty()) {
            addSomeLinkki(linkedin, SoMeLinkkiTyyppiTyyppi.LINKED_IN);
        }
        if (googlePlus != null && !googlePlus.isEmpty()) {
            addSomeLinkki(googlePlus, SoMeLinkkiTyyppiTyyppi.GOOGLE_PLUS);
        }
        if (muu1 != null && !muu1.isEmpty()) {
            addSomeLinkki(muu1, SoMeLinkkiTyyppiTyyppi.MUU);
        }
        if (muu2 != null && !muu2.isEmpty()) {
            addSomeLinkki(muu2, SoMeLinkkiTyyppiTyyppi.MUU);
        }
    }
    
    private void convertVapaatKuvaukset() {
        this.tiedotDto.getVapaatKuvaukset().clear();
        for (KuvailevaTietoTyyppiTyyppi curTyyppi : KuvailevaTietoTyyppiTyyppi.values()) {
            KuvailevaTietoTyyppi kuvaus = convertKuvailevaTieto(curTyyppi);
            if (kuvaus != null) {
                this.tiedotDto.getVapaatKuvaukset().add(kuvaus);
            }
        }
    }
    
    private void addSomeLinkki(String linkki, SoMeLinkkiTyyppiTyyppi tyyppi) {
        SoMeLinkkiTyyppi linkkiDto = new SoMeLinkkiTyyppi();
        linkkiDto.setSisalto(linkki);
        linkkiDto.setTyyppi(tyyppi);
        this.tiedotDto.getSoMeLinkit().add(linkkiDto);
    }
    
    private KuvailevaTietoTyyppi convertKuvailevaTieto(KuvailevaTietoTyyppiTyyppi tyyppi) {
        List<Teksti> tekstis = new ArrayList<Teksti>();
        for (Map.Entry<String, LOPTiedotModel> curEntry : this.lopTiedot.entrySet()) {
            String curKuvaus = fetchKuvaus(curEntry.getValue(), tyyppi);
            if (curKuvaus != null && !curKuvaus.isEmpty()) {
                Teksti teksti = new Teksti();
                teksti.setKieliKoodi(curEntry.getKey());
                teksti.setValue(curKuvaus);
                tekstis.add(teksti);
            }
        }
        if (tekstis.isEmpty()) {
            return null;
        }
        MonikielinenTekstiTyyppi monikielinenTeksti = new MonikielinenTekstiTyyppi();
        monikielinenTeksti.getTeksti().addAll(tekstis);
        KuvailevaTietoTyyppi kuvailevaTieto = new KuvailevaTietoTyyppi();
        kuvailevaTieto.setTyyppi(tyyppi);
        kuvailevaTieto.setSisalto(monikielinenTeksti);
        return kuvailevaTieto;
    }
    
    private String fetchKuvaus(LOPTiedotModel lopTiedot, KuvailevaTietoTyyppiTyyppi tyyppi) {
        for (LOPTietoModel curLopTieto : lopTiedot.getTiedot()) {
            if (curLopTieto.getTyyppi().value().equals(tyyppi.value())) {
                return curLopTieto.getKuvaus();
            }
        }
        return null;
    }
    
    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getLinkedin() {
        return linkedin;
    }

    public void setLinkedin(String linkedin) {
        this.linkedin = linkedin;
    }

    public String getGooglePlus() {
        return googlePlus;
    }

    public void setGooglePlus(String googlePlus) {
        this.googlePlus = googlePlus;
    }

    public String getMuu1() {
        return muu1;
    }

    public void setMuu1(String muu1) {
        this.muu1 = muu1;
    }

    public String getMuu2() {
        return muu2;
    }

    public void setMuu2(String muu2) {
        this.muu2 = muu2;
    }

    public OrganisaatioKuvaModel getKuva() {
        return kuva;
    }

    public void setKuva(OrganisaatioKuvaModel kuva) {
        this.kuva = kuva;
    }
    
    public OsoiteDTO getPostiosoite() {
        return postiosoite;
    }

    public void setPostiosoite(OsoiteDTO postiosoite) {
        this.postiosoite = postiosoite;
    }

    public OsoiteDTO getKayntiosoite() {
        return kayntiosoite;
    }

    public void setKayntiosoite(OsoiteDTO kayntiosoite) {
        this.kayntiosoite = kayntiosoite;
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
    
    public OsoiteDTO getEnglantiKayntiOsoite() {
        return englantiKayntiOsoite;
    }

    public void setEnglantiKayntiOsoite(OsoiteDTO englantiKayntiOsoite) {
        this.englantiKayntiOsoite = englantiKayntiOsoite;
    }

    public OsoiteDTO getEnglantiPostiOsoite() {
        return englantiPostiOsoite;
    }

    public void setEnglantiPostiOsoite(OsoiteDTO englantiPostiOsoite) {
        this.englantiPostiOsoite = englantiPostiOsoite;
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

    public EmailDTO getEmail() {
        return email;
    }

    public void setEmail(EmailDTO email) {
        this.email = email;
    }

    public String getOtNimiFi() {
        return otNimiFi;
    }
    
    /**
    *
    * @return
    */
   public MultiLingualTextImpl getOtNimi() {
       return otNimi;
   }

   /**
    *
    * @param otNimi
    */
   public void setOtNimi(MultiLingualTextImpl otNimi) {
       this.otNimi = otNimi;
   }

   public String getOtNimiSv() {
       return otNimiSv;
   }

   public void setOtNimiSv(String otNimiSv) {
       this.otNimiSv = otNimiSv;
   }

   public String getOtNimiEn() {
       return otNimiEn;
   }

   public void setOtNimiEn(String otNimiEn) {
       this.otNimiEn = otNimiEn;
   }

   public void setOtNimiFi(String otNimiFi) {
       this.otNimiFi = otNimiFi;
   }
   

   public YhteyshenkiloModel getEctsYhteyshenkilo() {
       return ectsYhteyshenkilo;
   }

   public void setEctsYhteyshenkilo(YhteyshenkiloModel ectsYhteyshenkilo) {
       this.ectsYhteyshenkilo = ectsYhteyshenkilo;
   }

   public String getWwwOsoite() {
	   return wwwOsoite==null ? null : wwwOsoite.getWwwOsoite();
   }
   public void setWwwOsoite(String wwwOsoite) {
	   if (this.wwwOsoite == null) {
		   this.wwwOsoite = new WwwDTO();
	   }
	   this.wwwOsoite.setWwwOsoite(wwwOsoite);
   }
   public String getPuhelinnumero() {
       return (this.puhelin != null) ? this.puhelin.getPuhelinnumero() : null;
   }
    
   public void setPuhelinnumero(String puhelinnnumero) {
       if (this.puhelin == null) {
           this.puhelin = new PuhelinnumeroDTO();
           this.puhelin.setTyyppi(PuhelinNumeroTyyppi.PUHELIN);
       }
       this.puhelin.setPuhelinnumero(puhelinnnumero);
   }
   
   public String getFaksinumero() {
       return (this.faksi != null) ? this.faksi.getPuhelinnumero() : null;
   }
   
   public void setFaksinumero(String puhelinnnumero) {
       if (this.faksi == null) {
           this.faksi = new PuhelinnumeroDTO();
           this.faksi.setTyyppi(PuhelinNumeroTyyppi.FAKSI);
       }
       this.faksi.setPuhelinnumero(puhelinnnumero);
   }
   
   public String getEmailOsoite() {
       return (this.email != null) ? this.email.getEmail() : null;
   }
   
   public void setEmailOsoite(String emailOsoite) {
       if (this.email == null) {
           this.email = new EmailDTO();
       }
       this.email.setEmail(emailOsoite);
   }

    private EmailDTO getEmailFromYts() {
        EmailDTO email = new EmailDTO();
        if (tiedotDto.getHakutoimisto() == null || tiedotDto.getHakutoimisto().getOpintotoimistoYhteystiedot().isEmpty()) {
            
            return email;
        }
        for (YhteystietoDTO curYt : tiedotDto.getHakutoimisto().getOpintotoimistoYhteystiedot()) {
            if (curYt instanceof EmailDTO) {
                return (EmailDTO)curYt;
            }
        }
        
        return email;
    }
    
    private OsoiteDTO getOsoiteByTyyppi(OsoiteTyyppi tyyppi) {
        OsoiteDTO osoite = new OsoiteDTO();
        osoite.setOsoiteTyyppi(tyyppi);
        if (tiedotDto.getHakutoimisto() == null || tiedotDto.getHakutoimisto().getOpintotoimistoYhteystiedot().isEmpty()) {
            
            return osoite;
        }
        for (YhteystietoDTO curYt : tiedotDto.getHakutoimisto().getOpintotoimistoYhteystiedot()) {
            if (curYt instanceof OsoiteDTO && ((OsoiteDTO)curYt).getOsoiteTyyppi().equals(tyyppi)) {
                return (OsoiteDTO)curYt;
            }
        }
        
        return osoite;
    }
    
    private PuhelinnumeroDTO getPuhelinnumeroByTyyppi(PuhelinNumeroTyyppi tyyppi) {
        PuhelinnumeroDTO puhelin = new PuhelinnumeroDTO();
        puhelin.setTyyppi(tyyppi);
        if (tiedotDto.getHakutoimisto() == null || tiedotDto.getHakutoimisto().getOpintotoimistoYhteystiedot().isEmpty()) {
            
            return puhelin;
        }
        for (YhteystietoDTO curYt : tiedotDto.getHakutoimisto().getOpintotoimistoYhteystiedot()) {
            if (curYt instanceof PuhelinnumeroDTO && ((PuhelinnumeroDTO)curYt).getTyyppi().equals(tyyppi)) {
                return (PuhelinnumeroDTO)curYt;
            }
        }
       
        return puhelin;
    }
    
    public List<KielikaannosModel> getAdditionalNimet() {
        return additionalNimet;
    }

    public void setAdditionalNimet(List<KielikaannosModel> additionalNimet) {
        this.additionalNimet = additionalNimet;
    }

    public boolean isYksiNimi() {
        return yksiNimi;
    }

    public void setYksiNimi(boolean yksiNimi) {
        this.yksiNimi = yksiNimi;
    }
   
}
