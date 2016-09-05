
package fi.vm.sade.organisaatio.api.model.types;

public enum SoMeLinkkiTyyppiTyyppi {
    FACEBOOK("Facebook"),
    LINKED_IN("LinkedIn"),
    TWITTER("Twitter"),
    GOOGLE_PLUS("GooglePlus"),
    MUU("Muu");
    
    private final String value;

    SoMeLinkkiTyyppiTyyppi(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SoMeLinkkiTyyppiTyyppi fromValue(String v) {
        for (SoMeLinkkiTyyppiTyyppi c: SoMeLinkkiTyyppiTyyppi.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
