/*
 *
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 */

package fi.vm.sade.organisaatio.resource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import fi.vm.sade.organisaatio.api.model.types.YhteystietojenTyyppiDTO;
import fi.vm.sade.organisaatio.dao.YhteystietojenTyyppiDAOImpl;
import fi.vm.sade.organisaatio.model.YhteystietojenTyyppi;
import fi.vm.sade.organisaatio.service.NotAuthorizedException;
import fi.vm.sade.organisaatio.service.auth.PermissionChecker;
import fi.vm.sade.organisaatio.service.converter.ConverterFactory;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

/**
 *
 */
@Path("/yhteystietojentyyppi")
@Component("yhteystietojenTyyppiResource")
@Api(value = "/yhteystietojentyyppi", description = "Yhteytietojen tyyppeihin liittyvät operaatiot")
public class YhteystietojenTyyppiResource {
    @Autowired
    private YhteystietojenTyyppiDAOImpl yhteystietojenTyyppiDAO;

    @Autowired
    private ConverterFactory converterFactory;

    @Autowired
    PermissionChecker permissionChecker;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Palauttaa yhteystietotyypit", notes = "Palauttaa yhteystietotyypit", response = List.class)
    public List<YhteystietojenTyyppiDTO> getYhteystietoTyypit() {
        List<YhteystietojenTyyppiDTO> tyypit = new ArrayList<YhteystietojenTyyppiDTO>();
        for (YhteystietojenTyyppi t : yhteystietojenTyyppiDAO.findAll()) {
            tyypit.add((YhteystietojenTyyppiDTO)converterFactory.convertToDTO(t));
        }
        return tyypit;
    }

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Muokkaa yhteystietotyyppiä", notes = "Palauttaa yhteystietotyyppiä", response = List.class)
    @Secured({"ROLE_APP_ORGANISAATIOHALLINTA"})
    public YhteystietojenTyyppiDTO updateYhteystietoTyyppi(YhteystietojenTyyppiDTO dto) {
        try {
            permissionChecker.checkEditYhteystietojentyyppi();
        } catch (NotAuthorizedException nae) {
            throw new OrganisaatioResourceException(Response.Status.FORBIDDEN, nae.toString());
        }
        YhteystietojenTyyppi entity = converterFactory.convertYhteystietojenTyyppiToJPA(dto, true);
        if (entity == null) {
            throw new OrganisaatioResourceException(Response.Status.BAD_REQUEST, "Entity is null.");
        }
        yhteystietojenTyyppiDAO.update(entity);
        return (YhteystietojenTyyppiDTO)converterFactory.convertToDTO(entity);
    }

}
