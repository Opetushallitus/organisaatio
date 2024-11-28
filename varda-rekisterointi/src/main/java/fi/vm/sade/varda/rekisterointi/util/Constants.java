package fi.vm.sade.varda.rekisterointi.util;

public final class Constants {

    private Constants() {
    }

    public static final String SESSION_ATTRIBUTE_NAME_BUSINESS_ID = "businessId";
    public static final String SESSION_ATTRIBUTE_NAME_ORGANISATION_NAME = "organisationName";
    public static final String SESSION_ATTRIBUTE_NAME_SESSION_ID = "sessionId";
    public static final String SESSION_ATTRIBUTE_NAME_CALLBACK_URL = "callbackUrl";

    public static final String CALLER_ID = "1.2.246.562.10.00000000001.varda-rekisterointi";

    public static final String VIRKAILIJA_ROLE = "APP_ORGANISAATIOIDEN_REKISTEROITYMINEN_OPH";
    public static final String VARDA_ROLE = "APP_ORGANISAATIOIDEN_REKISTEROITYMINEN_VARDA";
    public static final String JOTPA_ROLE = "APP_ORGANISAATIOIDEN_REKISTEROITYMINEN_JOTPA";
    public static final String[] VIRKAILIJA_UI_ROLES = new String[]{VIRKAILIJA_ROLE, VARDA_ROLE, JOTPA_ROLE};
    public static final String VIRKAILIJA_PRE_AUTH = "hasAnyRole('" + VIRKAILIJA_ROLE + "','" + VARDA_ROLE + "','" + JOTPA_ROLE + "')";

    public static final String PAAKAYTTAJA_ROLE = VIRKAILIJA_ROLE + "_1.2.246.562.10.00000000001";
    public static final String PAAKAYTTAJA_AUTHORITY = "ROLE_" + PAAKAYTTAJA_ROLE;

}
