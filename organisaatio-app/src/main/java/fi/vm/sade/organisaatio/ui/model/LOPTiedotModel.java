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
import java.util.List;

import fi.vm.sade.organisaatio.api.model.types.KuvailevaTietoTyyppiTyyppi;

/**
 * 
 * @author Markus
 */
public class LOPTiedotModel {
    
    private List<LOPTietoModel> tiedot = new ArrayList<LOPTietoModel>(); 
    
    public void clearKuvailevatTiedot() {
        tiedot.remove(this.getTietoObject(KuvailevaTietoTyyppiTyyppi.YLEISKUVAUS));
        tiedot.remove(this.getTietoObject(KuvailevaTietoTyyppiTyyppi.ESTEETOMYYS));
        tiedot.remove(this.getTietoObject(KuvailevaTietoTyyppiTyyppi.OPPIMISYMPARISTO));
        tiedot.remove(this.getTietoObject(KuvailevaTietoTyyppiTyyppi.VUOSIKELLO));
        tiedot.remove(this.getTietoObject(KuvailevaTietoTyyppiTyyppi.VASTUUHENKILOT));
        tiedot.remove(this.getTietoObject(KuvailevaTietoTyyppiTyyppi.VALINTAMENETTELY));
        tiedot.remove(this.getTietoObject(KuvailevaTietoTyyppiTyyppi.KIELIOPINNOT));
        tiedot.remove(this.getTietoObject(KuvailevaTietoTyyppiTyyppi.TYOHARJOITTELU));
        tiedot.remove(this.getTietoObject(KuvailevaTietoTyyppiTyyppi.OPISKELIJALIIKKUVUUS));
        tiedot.remove(this.getTietoObject(KuvailevaTietoTyyppiTyyppi.KANSAINVALISET_KOULUTUSOHJELMAT));
    }
    
    public void clearPalvelutOppijalleTiedot() {
        tiedot.remove(this.getTietoObject(KuvailevaTietoTyyppiTyyppi.KUSTANNUKSET));
        tiedot.remove(this.getTietoObject(KuvailevaTietoTyyppiTyyppi.RAHOITUS));
        tiedot.remove(this.getTietoObject(KuvailevaTietoTyyppiTyyppi.OPISKELIJARUOKAILU));
        tiedot.remove(this.getTietoObject(KuvailevaTietoTyyppiTyyppi.TERVEYDENHUOLTOPALVELUT));
        tiedot.remove(this.getTietoObject(KuvailevaTietoTyyppiTyyppi.VAKUUTUKSET));
        tiedot.remove(this.getTietoObject(KuvailevaTietoTyyppiTyyppi.OPISKELIJALIIKUNTA));
        tiedot.remove(this.getTietoObject(KuvailevaTietoTyyppiTyyppi.VAPAA_AIKA));
        tiedot.remove(this.getTietoObject(KuvailevaTietoTyyppiTyyppi.OPISKELIJA_JARJESTOT));
        tiedot.remove(this.getTietoObject(KuvailevaTietoTyyppiTyyppi.TIETOA_ASUMISESTA));
    }
    
    public String getTietoaAsumisesta() {
        return getTieto(KuvailevaTietoTyyppiTyyppi.TIETOA_ASUMISESTA);
    }
    
    public void setTietoaAsumisesta(String kuvaus) {
        setTieto(KuvailevaTietoTyyppiTyyppi.TIETOA_ASUMISESTA, kuvaus);
    }
    
    public String getYleiskuvaus() {
        return getTieto(KuvailevaTietoTyyppiTyyppi.YLEISKUVAUS);
    }
    
    public void setYleiskuvaus(String kuvaus) {
        setTieto(KuvailevaTietoTyyppiTyyppi.YLEISKUVAUS, kuvaus);
    }
    
    public String getEsteettomyys() {
        return getTieto(KuvailevaTietoTyyppiTyyppi.ESTEETOMYYS);
    }
    
    public void setEsteettomyys(String kuvaus) {
         setTieto(KuvailevaTietoTyyppiTyyppi.ESTEETOMYYS, kuvaus);
    }
    
    public String getOppimisymparisto() {
        return getTieto(KuvailevaTietoTyyppiTyyppi.OPPIMISYMPARISTO);
    }
    
    public void setOppimisymparisto(String kuvaus) {
        setTieto(KuvailevaTietoTyyppiTyyppi.OPPIMISYMPARISTO, kuvaus);
    }
    
    public String getVuosikello() {
        return getTieto(KuvailevaTietoTyyppiTyyppi.VUOSIKELLO);
    }
    
    public void setVuosikello(String kuvaus) {
        setTieto(KuvailevaTietoTyyppiTyyppi.VUOSIKELLO, kuvaus);
    }
    
    public String getVastuuhenkilot() {
        return getTieto(KuvailevaTietoTyyppiTyyppi.VASTUUHENKILOT);
    }
    
    public void setVastuuhenkilot(String kuvaus) {
        setTieto(KuvailevaTietoTyyppiTyyppi.VASTUUHENKILOT, kuvaus);
    }
    
    public String getValintamenettely() {
        return getTieto(KuvailevaTietoTyyppiTyyppi.VALINTAMENETTELY);
    }
    
    public void setValintamenettely(String kuvaus) {
        setTieto(KuvailevaTietoTyyppiTyyppi.VALINTAMENETTELY, kuvaus);
    }
    
    public String getAiempiOsaaminen() {
        return getTieto(KuvailevaTietoTyyppiTyyppi.AIEMMIN_HANKITTU_OSAAMINEN);
    }
    
    public void setAiempiOsaaminen(String kuvaus) {
        setTieto(KuvailevaTietoTyyppiTyyppi.AIEMMIN_HANKITTU_OSAAMINEN, kuvaus);
    }
    
    public String getKieliopinnot() {
        return getTieto(KuvailevaTietoTyyppiTyyppi.KIELIOPINNOT);
    }
    
    public void setKieliopinnot(String kuvaus) {
        setTieto(KuvailevaTietoTyyppiTyyppi.KIELIOPINNOT, kuvaus);
    }
    
    public String getTyoharjoittelu() {
        return getTieto(KuvailevaTietoTyyppiTyyppi.TYOHARJOITTELU);
    }
    
    public void setTyoharjoittelu(String kuvaus) {
        setTieto(KuvailevaTietoTyyppiTyyppi.TYOHARJOITTELU, kuvaus);
    }
    
    public String getOpiskelijaliikkuvuus() {
        return getTieto(KuvailevaTietoTyyppiTyyppi.OPISKELIJALIIKKUVUUS);
    }
    
    public void setOpiskelijaliikkuvuus(String kuvaus) {
        setTieto(KuvailevaTietoTyyppiTyyppi.OPISKELIJALIIKKUVUUS, kuvaus);
    }
    
    public String getKansainvalisyys() {
        return getTieto(KuvailevaTietoTyyppiTyyppi.KANSAINVALISET_KOULUTUSOHJELMAT);
    }
    
    public void setKansainvalisyys(String kuvaus) {
        setTieto(KuvailevaTietoTyyppiTyyppi.KANSAINVALISET_KOULUTUSOHJELMAT, kuvaus);
    }
    
    public String getKustannukset() {
        return getTieto(KuvailevaTietoTyyppiTyyppi.KUSTANNUKSET);
    }
    
    public void setKustannukset(String kuvaus) {
        setTieto(KuvailevaTietoTyyppiTyyppi.KUSTANNUKSET, kuvaus);
    }
    
    public String getRahoitus() {
        return getTieto(KuvailevaTietoTyyppiTyyppi.RAHOITUS);
    }
    
    public void setRahoitus(String kuvaus) {
        setTieto(KuvailevaTietoTyyppiTyyppi.RAHOITUS, kuvaus);
    }
    
    public String getOpiskelijaruokailu() {
        return getTieto(KuvailevaTietoTyyppiTyyppi.OPISKELIJARUOKAILU);
    }
    
    public void setOpiskelijaruokailu(String kuvaus) {
        setTieto(KuvailevaTietoTyyppiTyyppi.OPISKELIJARUOKAILU, kuvaus);
    }
    
    public String getTerveydenhuolto() {
        return getTieto(KuvailevaTietoTyyppiTyyppi.TERVEYDENHUOLTOPALVELUT);
    }
    
    public void setTerveydenhuolto(String kuvaus) {
        setTieto(KuvailevaTietoTyyppiTyyppi.TERVEYDENHUOLTOPALVELUT, kuvaus);
    }
    
    public String getVakuutukset() {
        return getTieto(KuvailevaTietoTyyppiTyyppi.VAKUUTUKSET);
    }
    
    public void setVakuutukset(String kuvaus) {
        setTieto(KuvailevaTietoTyyppiTyyppi.VAKUUTUKSET, kuvaus);
    }
    
    public String getOpiskelijaliikunta() {
        return getTieto(KuvailevaTietoTyyppiTyyppi.OPISKELIJALIIKUNTA);
    }
    
    public void setOpiskelijaliikunta(String kuvaus) {
        setTieto(KuvailevaTietoTyyppiTyyppi.OPISKELIJALIIKUNTA, kuvaus);
    }
    
    public String getVapaaAika() {
        return getTieto(KuvailevaTietoTyyppiTyyppi.VAPAA_AIKA);
    }
    
    public List<LOPTietoModel> getTiedot() {
        return tiedot;
    }

    public void setTiedot(List<LOPTietoModel> tiedot) {
        this.tiedot = tiedot;
    }

    public void setVapaaAika(String kuvaus) {
        setTieto(KuvailevaTietoTyyppiTyyppi.VAPAA_AIKA, kuvaus);
    }
    
    public String getOpiskelijaJarjestot() {
        return getTieto(KuvailevaTietoTyyppiTyyppi.OPISKELIJA_JARJESTOT);
    }
    
    public void setOpiskelijaJarjestot(String kuvaus) {
        setTieto(KuvailevaTietoTyyppiTyyppi.OPISKELIJA_JARJESTOT, kuvaus);
    }
    
    private void setTieto(KuvailevaTietoTyyppiTyyppi tyyppi, String kuvaus) {
        for (LOPTietoModel curTieto : tiedot) {
            if (curTieto.getTyyppi().value().equals(tyyppi.value())) {
                curTieto.setKuvaus(kuvaus);
                return;
            }
        }
        LOPTietoModel tieto = new LOPTietoModel(tyyppi, kuvaus);
        tiedot.add(tieto);
        
    }
    
    private String getTieto(KuvailevaTietoTyyppiTyyppi tyyppi) {
        for (LOPTietoModel curTieto : tiedot) {
            if (curTieto.getTyyppi().value().equals(tyyppi.value())) {
                return curTieto.getKuvaus();
            }
        }
        return null;
    }
    
    private LOPTietoModel getTietoObject(KuvailevaTietoTyyppiTyyppi tyyppi) {
        for (LOPTietoModel curTieto : tiedot) {
            if (curTieto.getTyyppi().value().equals(tyyppi.value())) {
                return curTieto;
            }
        }
        return null;
    }

}
