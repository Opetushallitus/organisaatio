package fi.vm.sade.organisaatio.api;


public interface OrganisaatioValidationConstraints {
	
	static final String YTUNNUS_PATTERN = "\\d\\d\\d\\d\\d\\d\\d-\\d";
	static final String VIRASTOTUNNUS_PATTERN = "\\d\\d\\d\\d\\d\\d.*";
	
}
