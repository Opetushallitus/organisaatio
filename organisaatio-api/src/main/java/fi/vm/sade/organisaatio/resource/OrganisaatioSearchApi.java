package fi.vm.sade.organisaatio.resource;

import com.fasterxml.jackson.annotation.JsonView;
import fi.vm.sade.organisaatio.api.views.Views;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioHakutulosV4;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioPerustietoV4;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioSearchCriteriaDTOV4;
import fi.vm.sade.organisaatio.resource.dto.HakuTulos;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;

@Validated
public interface OrganisaatioSearchApi {

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    OrganisaatioHakutulosV4 searchOrganisaatiot(@ParameterObject() OrganisaatioSearchCriteriaDTOV4 hakuEhdot);
    @GetMapping(path = "/nimi", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(Views.Nimi.class)
    HakuTulos<OrganisaatioPerustietoV4> searchOrganisaatiotNimet(OrganisaatioSearchCriteriaDTOV4 hakuEhdot);
    @GetMapping(path = "/tyyppi", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(Views.Tyyppi.class)
    HakuTulos<OrganisaatioPerustietoV4> searchOrganisaatiotTyypit(OrganisaatioSearchCriteriaDTOV4 hakuEhdot);
}
