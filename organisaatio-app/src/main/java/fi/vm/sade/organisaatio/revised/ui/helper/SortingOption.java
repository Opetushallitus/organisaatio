package fi.vm.sade.organisaatio.revised.ui.helper;

public enum SortingOption {
    
    KOULUTUSTOIMIJA_AAKKOSITTAIN("c_koultoimAakkosittain"),
    
    AAKKOSITTAIN("c_aakkosittain"),
    
    ORGANISAATIOTYYPEITTAIN("c_organisaatiotyypeittain"),
   
    KUNNITTAIN("c_kunnittain");
    
    private final String value;

    SortingOption(String v) {
        value = v;
    }

    public String value() {
        return value;
    }
}
