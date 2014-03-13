/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.vm.sade.organisaatio.service.util;

import fi.vm.sade.organisaatio.model.Osoite;
import fi.vm.sade.organisaatio.model.Yhteystieto;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author simok
 */
public abstract class YhteystietoUtil {
    public static boolean isPostiOsoite(Osoite osoite) {
        return osoite.getOsoiteTyyppi().equals(Osoite.TYYPPI_POSTIOSOITE) || 
                osoite.getOsoiteTyyppi().equals(Osoite.TYYPPI_ULKOMAINEN_POSTIOSOITE);
    }

    public static boolean isKayntiOsoite(Osoite osoite) {
        return osoite.getOsoiteTyyppi().equals(Osoite.TYYPPI_KAYNTIOSOITE) || 
                osoite.getOsoiteTyyppi().equals(Osoite.TYYPPI_ULKOMAINEN_KAYNTIOSOITE);
    }

    
    public static List<Osoite> getPostiOsoitteet(List<Yhteystieto> yhteystiedot) {
        List<Osoite> postiOsoitteet = new ArrayList<Osoite>();

        for (Yhteystieto yhteystieto : yhteystiedot) {
            if (yhteystieto instanceof Osoite) {
                Osoite osoite = (Osoite) yhteystieto;
                if (isPostiOsoite(osoite)) {
                    postiOsoitteet.add(osoite);
                }
            }
        }
        return postiOsoitteet;
    }

    public static List<Osoite> getKayntiOsoitteet(List<Yhteystieto> yhteystiedot) {
        List<Osoite> kayntiOsoitteet = new ArrayList<Osoite>();

        for (Yhteystieto yhteystieto : yhteystiedot) {
            if (yhteystieto instanceof Osoite) {
                Osoite osoite = (Osoite) yhteystieto;
                if (isKayntiOsoite(osoite)) {
                    kayntiOsoitteet.add(osoite);
                }
            }
        }
        return kayntiOsoitteet;
    }

}
