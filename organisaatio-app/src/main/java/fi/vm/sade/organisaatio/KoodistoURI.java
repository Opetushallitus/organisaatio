package fi.vm.sade.organisaatio;

import org.springframework.beans.factory.annotation.Configurable;

/**
 * @author Antti Salonen
 */
@Configurable
public class KoodistoURI {

    public static String KOODISTO_POSTINUMERO_URI = "posti";
    public static String KOODISTO_MAA_URI = "maat kaksimerkkisellä arvolla";
    private static String KOODISTO_YRITYS_URI = "http://yritys";
    public static String KOODISTO_KIELI_URI = "KIELI";
    public static String KOODISTO_KOTIPAIKKA_URI = "KUNTA";
    public static String KOODISTO_OPPILAITOSTYYPPI_URI = "Oppilaitostyyppi";
    public static String KOODISTO_VUOSILUOKAT_URI = "vuosiluokat";

    public String getPostinumeroUri() {
        return KOODISTO_POSTINUMERO_URI;

    }

    public void setPostinumeroUri(String postinumeroUri) {
        if (postinumeroUri != null && postinumeroUri.length() > 1) {
            KoodistoURI.KOODISTO_POSTINUMERO_URI = postinumeroUri;
        }
    }

    public String getMaaUri() {
        return KOODISTO_MAA_URI;

    }

    public void setMaaUri(String maaUri) {
        if (maaUri != null && maaUri.length() > 1) {
            KoodistoURI.KOODISTO_MAA_URI = maaUri;
        }
    }

    public String getYritysUri() {
        return KOODISTO_YRITYS_URI;

    }

    public void setYritysUri(String yritysUri) {
        if (yritysUri != null && yritysUri.length() > 1) {
            KoodistoURI.KOODISTO_YRITYS_URI = yritysUri;
        }
    }

    public String getKieliUri() {
        return KOODISTO_KIELI_URI;
    }

    public void setKieliUri(String kieliUri) {
        if (kieliUri != null && kieliUri.length() > 1) {
            KoodistoURI.KOODISTO_KIELI_URI = kieliUri;
        }
    }

    public String getKotipaikkaUri() {
        return KOODISTO_KOTIPAIKKA_URI;
    }

    public void setKotipaikkaUri(String kotipaikkaUri) {
        if (kotipaikkaUri != null && kotipaikkaUri.length() > 1) {
            KoodistoURI.KOODISTO_KOTIPAIKKA_URI = kotipaikkaUri;
        }
    }

    public String getOppilaitostyyppiUri() {
        return KOODISTO_OPPILAITOSTYYPPI_URI;
    }

    public void setOppilaitostyyppiUri(String oppilaitostyyppiUri) {
        if (oppilaitostyyppiUri != null && oppilaitostyyppiUri.length() > 1) {
            KoodistoURI.KOODISTO_OPPILAITOSTYYPPI_URI = oppilaitostyyppiUri;
        }
    }
    
    public String getVuosiluokatUri() {
        return KOODISTO_VUOSILUOKAT_URI;
    }
    
    public void setVuosiluokatUri(String vuosiluokatUri) {
        if (vuosiluokatUri != null && vuosiluokatUri.length() > 1) {
            KoodistoURI.KOODISTO_VUOSILUOKAT_URI = vuosiluokatUri;
        }
    }
    
    
}
