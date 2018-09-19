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


package fi.vm.sade.organisaatio.service.util;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.Organisaatio;
import org.apache.commons.lang.time.DateUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author simok
 */
public abstract class OrganisaatioUtil {
    public static boolean isRyhma(Organisaatio organisaatio) {
        return organisaatio.getTyypit().contains(OrganisaatioTyyppi.RYHMA.koodiValue());
    }

    public static boolean isOppilaitos(Organisaatio organisaatio) {
        return organisaatio.getTyypit() != null
                    && organisaatio.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS.koodiValue());
    }

    public static boolean isToimipiste(Organisaatio organisaatio) {
        return organisaatio.getTyypit() != null
                    && organisaatio.getTyypit().contains(OrganisaatioTyyppi.TOIMIPISTE.koodiValue());
    }

    /**
     * Organisaation lakkautuspvm -logiikka. Huom. kaikki parametrit voivat olla null.
     *
     * @param oldLpvm Päivitettävän organisaation nykyinen lakkautuspvm.
     * @param newLpvm Uusi lakkautuspvm.
     * @param origLpvm Päivitettävän organisaatiojoukun alkuperäinen lakkautuspvm.
     * @param parentLpvm Ylemmän tason organisaation lakkautuspvm.
     * @return
     */
    public static Date getUpdatedLakkautusPvm(Date oldLpvm, Date newLpvm, Date origLpvm, Date parentLpvm) {
        if (parentLpvm != null && (newLpvm == null || newLpvm.after(parentLpvm))) {
            newLpvm = parentLpvm;
        }
        if (origLpvm != null && !isSameDay(oldLpvm, origLpvm)) {
            return oldLpvm;
        } else {
            return newLpvm;
        }
    }

    /**
     * Tarkistetaan annetuista päivämääristä onko ne päivän osalta samat.
     *
     * @param pvm1 Päivämäärä lhs
     * @param pvm2 Päivämäärä rhs
     * @return true, jos päivät samat
     */
    public static boolean isSameDay(Date pvm1, Date pvm2) {
        // Täysin sama date tai molemmat null
        if (pvm1 == pvm2) {
            return true;
        }

        // Toinen null
        if (pvm1 == null || pvm2 == null) {
            return false;
        }

        // Kumpikaan ei ole null --> tarkastetaan onko sama päivä
        return DateUtils.isSameDay(pvm1, pvm2);
    }

    /**
     * Tarkistaa onko organisaatio aktiivinen.
     * @param o Organisaatio
     * @return true jos organisaatio on aktiivinen, false muuten
     */
    public static boolean isAktiivinen(Organisaatio o) {
        return !isPassive(o) && !isSuunniteltu(o);
    }

    /**
     * Tarkistaa onko organisaatio passivoitu eli lakkautettu
     * @param o Organisaatio
     * @return true jos organisaatio on passiivinen, false muuten
     */
    public static boolean isPassive(Organisaatio o) {
        return o.getLakkautusPvm() != null && o.getLakkautusPvm().before(new Date());
    }

    /**
     * Tarkistaa onko organisaatio suunniteltu, eli alkupvm tulevaisuudessa
     * @param o Organisaatio
     * @return true jos organisaatio on suunniteltu, false muuten
     */
    public static boolean isSuunniteltu(Organisaatio o) {
        return o.getAlkuPvm() != null && o.getAlkuPvm().after(new Date());
    }

    /**
     * Muuttaa organisaation tyypit koodistoarvoista organisaatiopalvelun vanhaan tyyppiin.
     * @param tyypitAsKoodi Organisaatiotyypit koodiarvoina
     * @return Organisaatiotyypit organisaatiopalvelun vanhassa muodossa
     */
    public static List<String> tyypitFromKoodis(List<String> tyypitAsKoodi) {
        return tyypitAsKoodi.stream()
                .map(tyyppi -> OrganisaatioTyyppi.fromKoodiValue(tyyppi).value())
                .collect(Collectors.toList());
    }

    /**
     * Muuttaa organisaation tyypit organisaatiopalvelun vanhasta tyypistä koodistoarvoiksi.
     * @param tyypit Organisaatiotyypit organisaatiopalvelun vanhassa muodossa
     * @return Organisaatiotyypit koodiarvoina
     */
    public static List<String> tyypitToKoodis(List<String> tyypit) {
        return tyypit.stream()
                .map(tyyppi -> OrganisaatioTyyppi.fromValue(tyyppi).koodiValue())
                .collect(Collectors.toList());
    }
}
