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

import fi.vm.sade.oid.service.ExceptionMessage;
import fi.vm.sade.oid.service.OIDService;
import fi.vm.sade.oid.service.types.NodeClassCode;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoElementtiDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietojenTyyppiDTO;
import fi.vm.sade.organisaatio.auth.PermissionChecker;
import fi.vm.sade.organisaatio.business.exception.NotAuthorizedException;
import fi.vm.sade.organisaatio.model.YhteystietoArvo;
import fi.vm.sade.organisaatio.model.YhteystietojenTyyppi;
import fi.vm.sade.organisaatio.repository.YhteystietoArvoRepository;
import fi.vm.sade.organisaatio.repository.YhteystietojenTyyppiRepository;
import fi.vm.sade.organisaatio.service.converter.ConverterFactory;
import fi.vm.sade.organisaatio.service.util.MonikielinenTekstiUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.List;

@ApiIgnore
@RestController
@RequestMapping("/yhteystietojentyyppi")
@Api(value = "/yhteystietojentyyppi", description = "Yhteytietojen tyyppeihin liittyvät operaatiot")
public class YhteystietojenTyyppiResource {
    @Autowired
    private YhteystietojenTyyppiRepository yhteystietojenTyyppiRepository;

    @Autowired
    private ConverterFactory converterFactory;

    @Autowired
    private PermissionChecker permissionChecker;

    @Autowired
    protected YhteystietoArvoRepository yhteystietoArvoRepository;

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

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Palauttaa yhteystietotyypit", notes = "Palauttaa yhteystietotyypit",
            response = YhteystietojenTyyppiDTO.class, responseContainer = "List")
    public List<YhteystietojenTyyppiDTO> getYhteystietoTyypit() {
        List<YhteystietojenTyyppiDTO> tyypit = new ArrayList<>();
        for (YhteystietojenTyyppi t : yhteystietojenTyyppiRepository.findAll()) {
            tyypit.add(converterFactory.convertToDTO(t));
        }
        return tyypit;
    }

    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Muokkaa yhteystietotyyppiä", notes = "Muokkaa yhteystietotyyppiä",
            response = YhteystietojenTyyppiDTO.class)
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
        } catch (ExceptionMessage em) {
            throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR, em.getMessage());
        }
        YhteystietojenTyyppi entity = converterFactory.convertYhteystietojenTyyppiToJPA(dto, true);
        if (entity == null) {
            throw new OrganisaatioResourceException(HttpStatus.BAD_REQUEST, "Entity is null.");
        }
        yhteystietojenTyyppiRepository.save(entity); //TODO works?
        return (YhteystietojenTyyppiDTO) converterFactory.convertToDTO(entity);
    }

    @PutMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Luo yhteystietotyyppi", notes = "Luo uusi yhteystietotyyppi", response = YhteystietojenTyyppiDTO.class)
    @Secured({"ROLE_APP_ORGANISAATIOHALLINTA"})
    @Transactional(rollbackFor = Throwable.class, readOnly = false)
    public YhteystietojenTyyppiDTO createYhteystietojenTyyppi(YhteystietojenTyyppiDTO yhteystietojenTyyppi) {
        try {
            permissionChecker.checkEditYhteystietojentyyppi();
        } catch (NotAuthorizedException nae) {
            throw new OrganisaatioResourceException(nae);
        }

        // Validate
        for (YhteystietojenTyyppi t : yhteystietojenTyyppiRepository.findAll()) {
            YhteystietojenTyyppiDTO dtd = converterFactory.convertToDTO(t);
            if (MonikielinenTekstiUtil.haveSameText(yhteystietojenTyyppi.getNimi(), dtd.getNimi())) {
                throw new OrganisaatioResourceException(HttpStatus.CONFLICT, "Duplicates not allowed.", "yhteystietojentyyppi.exception.duplicate");
            }
        }

        try {
            generateOids(yhteystietojenTyyppi);
        } catch (ExceptionMessage em) {
            throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR, em.getMessage());
        }
        YhteystietojenTyyppi entity = converterFactory.convertYhteystietojenTyyppiToJPA(yhteystietojenTyyppi, true);
        try {
            entity = this.yhteystietojenTyyppiRepository.save(entity);
        } catch (PersistenceException e) {
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, e.toString(), "yhteystietojentyyppi.exception.savefailed");
        }

        return converterFactory.convertToDTO(entity, YhteystietojenTyyppiDTO.class);
    }

    @DeleteMapping(path = "/{oid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Poista oidin yhteystietotyyppi", notes = "Poista oidin yhteystietotyyppi")
    @Secured({"ROLE_APP_ORGANISAATIOHALLINTA"})
    @Transactional(readOnly = false)
    public String deleteYhteystietottyypi(@PathVariable String oid, @RequestParam(defaultValue = "false") boolean force) {
        try {
            permissionChecker.checkEditYhteystietojentyyppi();
        } catch (NotAuthorizedException nae) {
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae.toString());
        }

        List<YhteystietojenTyyppi> tyypit = this.yhteystietojenTyyppiRepository.findByOid(oid);
        if (tyypit.isEmpty()) {
            throw new OrganisaatioResourceException(
                    HttpStatus.NOT_FOUND,
                    oid,
                    "yhteystietojentyyppi.exception.remove.notfound"
            );
        }
        YhteystietojenTyyppi tyyppiToRemove = tyypit.get(0);
        List<YhteystietoArvo> arvos = yhteystietoArvoRepository.findByYhteystietojenTyyppi(tyyppiToRemove);
        if (force) {
            for (YhteystietoArvo arvo : arvos) {
                this.yhteystietoArvoRepository.delete(arvo);
            }
            this.yhteystietojenTyyppiRepository.delete(tyyppiToRemove);
        } else if (arvos.isEmpty()) {
            this.yhteystietojenTyyppiRepository.delete(tyyppiToRemove);
        } else {
            throw new OrganisaatioResourceException(
                    HttpStatus.CONFLICT,
                    oid,
                    "yhteystietojentyyppi.exception.remove.inuse"
            );
        }
        return "";
    }
}
