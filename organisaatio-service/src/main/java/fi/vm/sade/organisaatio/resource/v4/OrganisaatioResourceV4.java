package fi.vm.sade.organisaatio.resource.v4;

import fi.vm.sade.organisaatio.resource.OrganisaatioApi;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Hidden
public interface OrganisaatioResourceV4 extends OrganisaatioApi {
    @DeleteMapping(path = "/{oid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Poistaa oid:n määrittämän organisaation",
            description = "Operaatio poistaa organisaation annetulla oid:llä.")
    String deleteOrganisaatioOld(
            @Parameter(description = "Organisaation oid", required = true) @PathVariable String oid
    );
}
