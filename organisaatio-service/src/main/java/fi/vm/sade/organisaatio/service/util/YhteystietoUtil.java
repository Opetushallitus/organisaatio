package fi.vm.sade.organisaatio.service.util;

import fi.vm.sade.organisaatio.model.*;

import java.util.HashSet;
import java.util.Set;

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

    public static Set<Osoite> getPostiOsoitteet(Set<Yhteystieto> yhteystiedot) {
        Set<Osoite> postiOsoitteet = new HashSet<>();

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

    public static Set<Osoite> getKayntiOsoitteet(Set<Yhteystieto> yhteystiedot) {
        Set<Osoite> kayntiOsoitteet = new HashSet<>();

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

    public static Set<Www> getWwwOsoitteet(Set<Yhteystieto> yhteystiedot) {
        Set<Www> wwwOsoitteet = new HashSet<>();

        for (Yhteystieto yhteystieto : yhteystiedot) {
            if (yhteystieto instanceof Www) {
                Www osoite = (Www) yhteystieto;
                wwwOsoitteet.add(osoite);
            }
        }
        return wwwOsoitteet;
    }

    public static Set<Email> getEmailOsoitteet(Set<Yhteystieto> yhteystiedot) {
        Set<Email> emailOsoitteet = new HashSet<>();

        for (Yhteystieto yhteystieto : yhteystiedot) {
            if (yhteystieto instanceof Email) {
                Email osoite = (Email) yhteystieto;
                emailOsoitteet.add(osoite);
            }
        }
        return emailOsoitteet;
    }

    public static Set<Puhelinnumero> getPuhelinnumerot(Set<Yhteystieto> yhteystiedot) {
        Set<Puhelinnumero> puhelinnumerot = new HashSet<>();

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

}
