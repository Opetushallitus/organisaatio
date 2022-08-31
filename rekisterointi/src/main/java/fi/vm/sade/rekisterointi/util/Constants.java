package fi.vm.sade.rekisterointi.util;

public final class Constants {

  private Constants() {
  }

  public static final String SESSION_ATTRIBUTE_NAME_BUSINESS_ID = "businessId";
  public static final String SESSION_ATTRIBUTE_NAME_ORGANISATION_NAME = "organisationName";
  public static final String SESSION_ATTRIBUTE_NAME_SESSION_ID = "sessionId";
  public static final String SESSION_ATTRIBUTE_NAME_CALLBACK_URL = "callbackUrl";
  public static final String SESSION_ATTRIBUTE_NAME_ORIGINAL_REQUEST = "originalRequest";

  public static final String CALLER_ID = "1.2.246.562.10.00000000001.organisaatio-rekisterointi";

  public static final String VIRKAILIJA_ROLE = "APP_YKSITYISTEN_REKISTEROITYMINEN_CRUD";
  public static final String PAAKAYTTAJA_AUTHORITY = "ROLE_" + VIRKAILIJA_ROLE + "_1.2.246.562.10.00000000001";

}
