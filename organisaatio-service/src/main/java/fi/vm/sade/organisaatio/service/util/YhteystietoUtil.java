/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.vm.sade.organisaatio.service.util;

import fi.vm.sade.organisaatio.model.Email;
import fi.vm.sade.organisaatio.model.Osoite;
import fi.vm.sade.organisaatio.model.Puhelinnumero;
import fi.vm.sade.organisaatio.model.Www;
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

    public static boolean isPuhelinnumero(Puhelinnumero numero) {
        return numero.getTyyppi().equals(Puhelinnumero.TYYPPI_PUHELIN);
    }

    public static boolean isFaksinumero(Puhelinnumero numero) {
        return numero.getTyyppi().equals(Puhelinnumero.TYYPPI_FAKSI);
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

    public static List<Www> getWwwOsoitteet(List<Yhteystieto> yhteystiedot) {
        List<Www> wwwOsoitteet = new ArrayList<Www>();

        for (Yhteystieto yhteystieto : yhteystiedot) {
            if (yhteystieto instanceof Www) {
                Www osoite = (Www) yhteystieto;
                wwwOsoitteet.add(osoite);
            }
        }
        return wwwOsoitteet;
    }

    public static List<Email> getEmailOsoitteet(List<Yhteystieto> yhteystiedot) {
        List<Email> emailOsoitteet = new ArrayList<Email>();

        for (Yhteystieto yhteystieto : yhteystiedot) {
            if (yhteystieto instanceof Email) {
                Email osoite = (Email) yhteystieto;
                emailOsoitteet.add(osoite);
            }
        }
        return emailOsoitteet;
    }

    public static List<Puhelinnumero> getPuhelinnumerot(List<Yhteystieto> yhteystiedot) {
        List<Puhelinnumero> puhelinnumerot = new ArrayList<Puhelinnumero>();

        for (Yhteystieto yhteystieto : yhteystiedot) {
            if (yhteystieto instanceof Puhelinnumero) {
                Puhelinnumero numero = (Puhelinnumero) yhteystieto;
                if (isPuhelinnumero(numero)) {
                    puhelinnumerot.add(numero);
                }
            }
        }
        return puhelinnumerot;
    }

    public static List<Puhelinnumero> getFaksinumerot(List<Yhteystieto> yhteystiedot) {
        List<Puhelinnumero> faksinumerot = new ArrayList<Puhelinnumero>();

        for (Yhteystieto yhteystieto : yhteystiedot) {
            if (yhteystieto instanceof Puhelinnumero) {
                Puhelinnumero numero = (Puhelinnumero) yhteystieto;
                if (isFaksinumero(numero)) {
                    faksinumerot.add(numero);
                }
            }
        }
        return faksinumerot;
    }    
}
