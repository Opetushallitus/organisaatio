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
import fi.vm.sade.oid.service.ExceptionMessage;
import fi.vm.sade.oid.service.OIDService;
import fi.vm.sade.oid.service.types.NodeClassCode;
import fi.vm.sade.organisaatio.api.model.GenericFault;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoElementtiDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietojenTyyppiDTO;
import fi.vm.sade.organisaatio.dao.impl.YhteystietoArvoDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.YhteystietojenTyyppiDAOImpl;
import fi.vm.sade.organisaatio.model.YhteystietoArvo;
import fi.vm.sade.organisaatio.model.YhteystietojenTyyppi;
import fi.vm.sade.organisaatio.service.NotAuthorizedException;
import fi.vm.sade.organisaatio.service.auth.PermissionChecker;
import fi.vm.sade.organisaatio.service.converter.ConverterFactory;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.PersistenceException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    protected YhteystietoArvoDAOImpl yhteystietoArvoDAO;

    @Autowired
    private OIDService oidService;

    private void generateOids(YhteystietojenTyyppiDTO model) throws ExceptionMessage {
        if (model.getOid() == null) {
            model.setOid(this.oidService.newOid(NodeClassCode.TEKN_5));
        }
        for (YhteystietoElementtiDTO curYel : model.getAllLisatietokenttas()) {
            if (curYel != null && curYel.getOid() == null) {
                curYel.setOid(this.oidService.newOid(NodeClassCode.TEKN_5));
            }
        }
    }

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
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Muokkaa yhteystietotyyppiä", notes = "Muokkaa yhteystietotyyppiä", response = List.class)
    @Secured({"ROLE_APP_ORGANISAATIOHALLINTA"})
    @Transactional(readOnly = false)
    public YhteystietojenTyyppiDTO updateYhteystietoTyyppi(YhteystietojenTyyppiDTO dto) {
        try {
            permissionChecker.checkEditYhteystietojentyyppi();
        } catch (NotAuthorizedException nae) {
            throw new OrganisaatioResourceException(nae);
        }
        try {
            generateOids(dto);
        }
        catch (ExceptionMessage em) {
            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR, em.getMessage());
        }
        YhteystietojenTyyppi entity = converterFactory.convertYhteystietojenTyyppiToJPA(dto, true);
        if (entity == null) {
            throw new OrganisaatioResourceException(Response.Status.BAD_REQUEST, "Entity is null.");
        }
        yhteystietojenTyyppiDAO.update(entity);
        return (YhteystietojenTyyppiDTO)converterFactory.convertToDTO(entity);
    }

    @PUT
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Luo yhteystietotyyppi", notes = "Luo uusi yhteystietotyyppi")
    @Secured({"ROLE_APP_ORGANISAATIOHALLINTA"})
    @Transactional(rollbackFor = Throwable.class, readOnly = false)
    public YhteystietojenTyyppiDTO createYhteystietojenTyyppi(YhteystietojenTyyppiDTO yhteystietojenTyyppi) throws GenericFault {
        try {
            permissionChecker.checkEditYhteystietojentyyppi();
        } catch (NotAuthorizedException nae) {
            throw new OrganisaatioResourceException(nae);
        }
        try {
            generateOids(yhteystietojenTyyppi);
        } catch (ExceptionMessage em) {
            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR, em.getMessage());
        }
        YhteystietojenTyyppi entity = converterFactory.convertYhteystietojenTyyppiToJPA(yhteystietojenTyyppi, true);
        try {
            entity = this.yhteystietojenTyyppiDAO.insert(entity);
        } catch (PersistenceException e) {
            throw new OrganisaatioResourceException(Response.Status.FORBIDDEN, e.toString(), "yhteystietojentyyppi.exception.savefailed");
        }

        return converterFactory.convertToDTO(entity, YhteystietojenTyyppiDTO.class);
    }

    @DELETE
    @Path("/{oid}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Secured({"ROLE_APP_ORGANISAATIOHALLINTA"})
    @Transactional(readOnly = false)
    public String deleteYhteystietottyypi(@PathParam("oid") String oid) {
        try {
            permissionChecker.checkEditYhteystietojentyyppi();
        } catch (NotAuthorizedException nae) {
            throw new OrganisaatioResourceException(Response.Status.FORBIDDEN, nae.toString());
        }

        List<YhteystietojenTyyppi> tyypit = this.yhteystietojenTyyppiDAO.findBy("oid", oid);
        if (tyypit.isEmpty()) {
            throw new OrganisaatioResourceException(
                    Response.Status.NOT_FOUND,
                    oid,
                    "yhteystietojentyyppi.exception.remove.notfound"
            );
        }
        YhteystietojenTyyppi tyyppiToRemove = tyypit.get(0);
        List<YhteystietoArvo> arvos = yhteystietoArvoDAO.findByYhteystietojenTyyppi(tyyppiToRemove);
        if (arvos.isEmpty()) {
            this.yhteystietojenTyyppiDAO.remove(tyyppiToRemove);
        } else {
            throw new OrganisaatioResourceException(
                    Response.Status.CONFLICT,
                    oid,
                    "yhteystietojentyyppi.exception.remove.inuse"
            );
        }
        return "";
    }

}
