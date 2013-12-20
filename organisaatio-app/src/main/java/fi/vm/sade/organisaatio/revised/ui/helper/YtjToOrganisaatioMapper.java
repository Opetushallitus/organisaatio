package fi.vm.sade.organisaatio.revised.ui.helper;

import fi.vm.sade.organisaatio.api.model.types.EmailDTO;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OsoiteDTO;
import fi.vm.sade.organisaatio.api.model.types.OsoiteTyyppi;
import fi.vm.sade.organisaatio.api.model.types.PuhelinNumeroTyyppi;
import fi.vm.sade.organisaatio.api.model.types.PuhelinnumeroDTO;
import fi.vm.sade.organisaatio.api.model.types.WwwDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoDTO;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;
import fi.vm.sade.rajapinnat.ytj.api.YTJDTO;
import fi.vm.sade.rajapinnat.ytj.api.YTJOsoiteDTO;

import fi.vm.sade.organisaatio.KoodistoURI;

import java.text.SimpleDateFormat;
import java.util.Date;

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

/**
 *
 * @author Tuomas Katva
 */

public class YtjToOrganisaatioMapper {
    
    public static final String DATE_PATTERN = "dd.MM.yyyy";
    
    public static OrganisaatioDTO mapYtjToOrganisaatio(YTJDTO ytjDto, OrganisaatioDTO orgParam) {
        OrganisaatioDTO selectedOrganisaatio = orgParam;
        KoodistoHelper helper = new KoodistoHelper();
        if (selectedOrganisaatio != null && ytjDto != null) {
            String svNimi = ytjDto.getSvNimi();
            if (svNimi != null && svNimi.length() > 0 ) {
                selectedOrganisaatio.setNimi(setNimiValue("sv",svNimi));
            } else {
                selectedOrganisaatio.setNimi(setNimiValue("fi", ytjDto.getNimi()));//setNimiFi(ytjDto.getNimi());
            }
            selectedOrganisaatio.setYtunnus(ytjDto.getYtunnus());
            selectedOrganisaatio.setYritysmuoto(ytjDto.getYritysmuoto());
            selectedOrganisaatio.setKotipaikka(helper.tryGetKoodistoArvo(ytjDto.getKotiPaikkaKoodi(), KoodistoURI.KOODISTO_KOTIPAIKKA_URI));
            if (ytjDto.getYrityksenKieli() != null) {
                //This is terrible but what can you do about it...
                String kieli = tryGetKieliAbbr(ytjDto);

                if (kieli != null) {

                String foundKieli = helper.tryGetKoodistoArvo(kieli, KoodistoURI.KOODISTO_KIELI_URI);
                selectedOrganisaatio.getKielet().add(foundKieli);
                }
            }

            selectedOrganisaatio.setYtjPaivitysPvm(new Date());
            if (ytjDto.getKayntiOsoite() != null && ytjDto.getKayntiOsoite().getPostinumero() != null) {
                if (ytjDto.getKayntiOsoite().getKieli() == 1) {
                mapOsoiteYhteystieto(ytjDto.getKayntiOsoite(), OsoiteTyyppi.KAYNTI,selectedOrganisaatio);
                } else if (ytjDto.getKayntiOsoite().getKieli() == 2) {
                    mapOsoiteYhteystieto(ytjDto.getKayntiOsoite(), OsoiteTyyppi.RUOTSI_KAYNTI,selectedOrganisaatio);
                }
            }


            if (ytjDto.getPostiOsoite() != null && ytjDto.getPostiOsoite().getPostinumero() != null) {
                if (ytjDto.getPostiOsoite().getKieli() == 1) {
                mapOsoiteYhteystieto(ytjDto.getPostiOsoite(), OsoiteTyyppi.POSTI,selectedOrganisaatio);
                } else if (ytjDto.getPostiOsoite().getKieli() == 2) {
                    mapOsoiteYhteystieto(ytjDto.getPostiOsoite(), OsoiteTyyppi.RUOTSI_POSTI,selectedOrganisaatio);
                }
            }
            if (ytjDto.getSahkoposti() != null ) {
                mapSahkoposti(ytjDto.getSahkoposti(), selectedOrganisaatio);
            }
            if (ytjDto.getPuhelin() != null) {
                mapPuhelin(ytjDto.getPuhelin(),selectedOrganisaatio);
            }

            if (ytjDto.getFaksi() != null && ytjDto.getFaksi().trim().length() > 0) {
                mapFaksi(ytjDto.getFaksi(),selectedOrganisaatio);
            }

            if (ytjDto.getWww() != null) {
                mapWww(ytjDto.getWww(),selectedOrganisaatio);
            }
            if (ytjDto.getAloitusPvm() != null) {
                try {
                    SimpleDateFormat sdf  = new SimpleDateFormat(DATE_PATTERN);
                    selectedOrganisaatio.setAlkuPvm(sdf.parse(ytjDto.getAloitusPvm()));
                } catch (Exception exp) {

                }
            }

        }
        return selectedOrganisaatio;
    }

    private static String tryGetKieliAbbr(final YTJDTO ytjDto) {
        String kieli = null;
        kieli = ytjDto.getYrityksenKieli().trim().equalsIgnoreCase("suomi") ? "FI" : null;

        if (kieli == null ) {
            kieli = ytjDto.getYrityksenKieli().trim().equalsIgnoreCase("ruotsi") ? "SV" : null;
        }

        if (kieli == null) {
            kieli = ytjDto.getYrityksenKieli().trim().equalsIgnoreCase("englanti") ? "EN" : null;
        }

        return kieli;
    }
    
    private static MonikielinenTekstiTyyppi setNimiValue(String lang, String nimi) {
    	MonikielinenTekstiTyyppi nimiT = new MonikielinenTekstiTyyppi();
    	Teksti teksti = new Teksti();
    	teksti.setKieliKoodi(lang);
    	teksti.setValue(nimi);
    	nimiT.getTeksti().add(teksti);
    	return nimiT;
    }
    
    
    private static void mapSahkoposti(String ytjSposti, OrganisaatioDTO selectedOrganisaatio) {
        boolean emailFound = false;
        
        for (YhteystietoDTO yt:selectedOrganisaatio.getYhteystiedot()) {
            if (yt instanceof EmailDTO) {
                ((EmailDTO)yt).setEmail(ytjSposti);
                emailFound = true;
            }
        }
        
        if (!emailFound) {
            EmailDTO email = new EmailDTO();
            email.setEmail(ytjSposti);
            selectedOrganisaatio.getYhteystiedot().add(email);
        }
        
    }
    
    private static void mapWww(String www,OrganisaatioDTO selectedOrganisaatio) {
        boolean wwwFound = false;
        for (YhteystietoDTO yt:selectedOrganisaatio.getYhteystiedot()) {
            if (yt instanceof WwwDTO) {
                ((WwwDTO)yt).setWwwOsoite(www);
                wwwFound = true;
                        
            }
        }
        if (!wwwFound) {
            WwwDTO wwwDto = new WwwDTO();
            wwwDto.setWwwOsoite(www);
            selectedOrganisaatio.getYhteystiedot().add(wwwDto);
        }
    }

    private static void mapFaksi(String puhelin, OrganisaatioDTO selectedOrganisaatio)  {
        boolean puhelinFound = false;
        for (YhteystietoDTO yt:selectedOrganisaatio.getYhteystiedot()) {
            if (yt instanceof PuhelinnumeroDTO) {
                PuhelinnumeroDTO puh = (PuhelinnumeroDTO)yt;
                if (puh.getTyyppi().equals(PuhelinNumeroTyyppi.FAKSI)) {
                    puh.setPuhelinnumero(puhelin);
                    puhelinFound = true;
                }
            }
        }
        if (!puhelinFound) {
            PuhelinnumeroDTO puh = new PuhelinnumeroDTO();
            puh.setPuhelinnumero(puhelin);
            puh.setTyyppi(PuhelinNumeroTyyppi.FAKSI);
            selectedOrganisaatio.getYhteystiedot().add(puh);
        }
    }
    
    private static void mapPuhelin(String puhelin, OrganisaatioDTO selectedOrganisaatio) {
        boolean puhelinFound = false;
        for (YhteystietoDTO yt:selectedOrganisaatio.getYhteystiedot()) {
            if (yt instanceof PuhelinnumeroDTO) {
                PuhelinnumeroDTO puh = (PuhelinnumeroDTO)yt;
                if (puh.getTyyppi().equals(PuhelinNumeroTyyppi.PUHELIN)) {
                    puh.setPuhelinnumero(puhelin);
                    puhelinFound = true;
                }
            }
        }
        if (!puhelinFound) {
            PuhelinnumeroDTO puh = new PuhelinnumeroDTO();
            puh.setPuhelinnumero(puhelin);
            puh.setTyyppi(PuhelinNumeroTyyppi.PUHELIN);
            selectedOrganisaatio.getYhteystiedot().add(puh);
        }
    }
    
    private static void mapOsoiteYhteystieto(YTJOsoiteDTO ytjDto, OsoiteTyyppi tyyppi, OrganisaatioDTO selectedOrganisaatio) {
        boolean osoiteFound = false;
        KoodistoHelper helper = new KoodistoHelper();
        for (YhteystietoDTO yt:selectedOrganisaatio.getYhteystiedot()) {
            if (yt instanceof OsoiteDTO && ((OsoiteDTO)yt).getOsoiteTyyppi().equals(tyyppi) ) {
                ((OsoiteDTO)yt).setOsoite(ytjDto.getKatu());
                ((OsoiteDTO)yt).setPostinumero(helper.tryGetKoodistoArvo(ytjDto.getPostinumero(), KoodistoURI.KOODISTO_POSTINUMERO_URI));
                ((OsoiteDTO)yt).setPostitoimipaikka(ytjDto.getToimipaikka());
                ((OsoiteDTO)yt).setMaa(ytjDto.getMaa());
                osoiteFound = true;
            }
        }
        
        if (!osoiteFound) {
            OsoiteDTO osoite = new OsoiteDTO();
            osoite.setOsoite(ytjDto.getKatu());
            
            osoite.setPostinumero(helper.tryGetKoodistoArvo(ytjDto.getPostinumero(), KoodistoURI.KOODISTO_POSTINUMERO_URI));
            osoite.setPostitoimipaikka(ytjDto.getToimipaikka());
            osoite.setMaa(ytjDto.getMaa());
            osoite.setOsoiteTyyppi(tyyppi);
            osoite.setYtjPaivitysPvm(new Date());
            selectedOrganisaatio.getYhteystiedot().add(osoite);
        }
    }

   

}
