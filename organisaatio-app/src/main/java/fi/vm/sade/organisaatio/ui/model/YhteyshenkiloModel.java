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

import fi.vm.sade.organisaatio.api.model.types.YhteyshenkiloTyyppi;

/**
 * 
 * @author Markus
 */
public class YhteyshenkiloModel {
    
    private String oid;
    private String kokoNimi;
    private String titteli;
    private String email;
    private String puhelin;
    
    private YhteyshenkiloTyyppi yhteyshenkiloDto;
    
    YhteyshenkiloModel(YhteyshenkiloTyyppi yhteyshenkilo) {
        yhteyshenkiloDto = yhteyshenkilo != null ? yhteyshenkilo : new YhteyshenkiloTyyppi();
        oid = yhteyshenkiloDto.getOid();
        kokoNimi = yhteyshenkiloDto.getKokoNimi();
        titteli = yhteyshenkiloDto.getTitteli();
        email = yhteyshenkiloDto.getEmail();
        puhelin = yhteyshenkiloDto.getPuhelin();
    }
    
    YhteyshenkiloTyyppi convertToDto() {
        if (kokoNimi == null || kokoNimi.isEmpty()) {
            return null;
        }
        yhteyshenkiloDto.setOid(oid);
        yhteyshenkiloDto.setKokoNimi(kokoNimi);
        yhteyshenkiloDto.setTitteli(titteli);
        yhteyshenkiloDto.setEmail(email);
        yhteyshenkiloDto.setPuhelin(puhelin);
        return yhteyshenkiloDto;
    }
    
    public String getOid() {
        return oid;
    }
    public void setOid(String oid) {
        this.oid = oid;
    }
    public String getKokoNimi() {
        return kokoNimi;
    }
    public void setKokoNimi(String kokoNimi) {
        this.kokoNimi = kokoNimi;
    }
    public String getTitteli() {
        return titteli;
    }
    public void setTitteli(String titteli) {
        this.titteli = titteli;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPuhelin() {
        return puhelin;
    }
    public void setPuhelin(String puhelin) {
        this.puhelin = puhelin;
    }

}
