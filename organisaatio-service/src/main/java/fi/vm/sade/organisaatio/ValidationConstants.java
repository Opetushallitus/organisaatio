package fi.vm.sade.organisaatio;

public final class ValidationConstants {

    public static final int GENERIC_MIN = 3;
    public static final int GENERIC_MAX = 100;
    //public static final int YTUNNUS_LENGTH = 9;
    public static final int SHORT_MAX = 10;
    public static final int DESCRIPTION_MAX = 1000;
    public static final String EMAIL_PATTERN = "^[_A-Za-z0-9-+!#$%&'*/=?^`{|}~]+(\\.[_A-Za-z0-9-+!#$%&'*/=?^`{|}~]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

    public static final String ZIPCODE_PATTERN = "[0-9]{5}";
    public static final String PHONE_PATTERN = "[+|-| |\\(|\\)|[0-9]]{3,100}+";

    private ValidationConstants() {
    }

}
