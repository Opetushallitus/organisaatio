package fi.vm.sade.organisaatio.service.aspects;/*
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


import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import java.util.Date;
import java.util.List;

import fi.vm.sade.organisaatio.api.model.types.YhteystietojenTyyppiDTO;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioMuokkausTiedotDTO;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioMuokkausTulosListaDTO;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.resource.dto.ResultRDTO;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.restlet.resource.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import static fi.vm.sade.auditlog.organisaatio.LogMessage.builder;
import fi.vm.sade.auditlog.ApplicationType;
import fi.vm.sade.auditlog.organisaatio.OrganisaatioOperation;
import fi.vm.sade.auditlog.organisaatio.LogMessage;
import fi.vm.sade.auditlog.Audit;

/**
 * @author: Tuomas Katva Date: 9.8.2013
 */
@Aspect
public class AuditLogAspect {

    protected static final Logger LOG = LoggerFactory.getLogger(AuditLogAspect.class);

    public static final String serviceName = "Organisaatio-Service";
    public Audit audit = new Audit(serviceName, ApplicationType.VIRKAILIJA);

    // POST /organisaatio/{oid}
    @Around("execution(public * fi.vm.sade.organisaatio.resource.OrganisaatioResourceImpl.updateOrganisaatio(..))")
    private Object updateOrgAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        if (pjp.getArgs() != null && pjp.getArgs()[0] instanceof String) {
            String oid = ((String) pjp.getArgs()[0]);
            LogMessage logMessage = builder().id(getTekija()).setOperaatio(OrganisaatioOperation.ORG_UPDATE).oidList(oid).build();
            audit.log(logMessage);
        }
        else {
            LOG.warn("UNKNOWN PARAMETER IN AuditLogAspect updateOrgAdvice");
        }
        return result;
    }

    // DELETE /organisaatio/{oid}
    @Around("execution(public * fi.vm.sade.organisaatio.resource.OrganisaatioResourceImpl.deleteOrganisaatio(..))")
    private Object deleteOrgAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        if (pjp.getArgs() != null && pjp.getArgs()[0] instanceof String) {
            String oid = ((String) pjp.getArgs()[0]);
            LogMessage logMessage = builder().id(getTekija()).setOperaatio(OrganisaatioOperation.ORG_DELETE).oidList(oid).build();
            audit.log(logMessage);
        }
        else {
            LOG.warn("UNKNOWN PARAMETER IN AuditLogAspect deleteOrgAdvice");
        }
        return result;
    }
    // PUT /organisaatio/
    @Around("execution(public * fi.vm.sade.organisaatio.resource.OrganisaatioResourceImpl.newOrganisaatio(..))")
    private Object newOrgAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        if (result instanceof OrganisaatioRDTO) {
            OrganisaatioRDTO organisaatioRDTO = (OrganisaatioRDTO) result;
            LogMessage logMessage = builder().id(getTekija()).setOperaatio(OrganisaatioOperation.ORG_CREATE)
                    .oidList(organisaatioRDTO.getOid()).build();
            audit.log(logMessage);
        }
        else {
            LOG.warn("UNKNOWN PARAMETER IN AuditLogAspect newOrgAdvice");
        }
        return result;
    }
    // POST /yhteystietojentyyppi/
    @Around("execution(public * fi.vm.sade.organisaatio.resource.YhteystietojenTyyppiResource.updateYhteystietoTyyppi(..))")
    private Object updateYhtAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        if (pjp.getArgs() != null && pjp.getArgs()[0] instanceof YhteystietojenTyyppiDTO) {
            YhteystietojenTyyppiDTO yhteystietojenTyyppiDTO = ((YhteystietojenTyyppiDTO) pjp.getArgs()[0]);
            LogMessage logMessage = builder().id(getTekija()).setOperaatio(OrganisaatioOperation.YHTEYSTIETO_UPDATE)
                    .oidList(yhteystietojenTyyppiDTO.getOid()).build();
            audit.log(logMessage);
        }
        else {
            LOG.warn("UNKNOWN PARAMETER IN AuditLogAspect updateYhtAdvice");
        }
        return result;
    }
    // PUT /yhteystietojentyyppi/
    @Around("execution(public * fi.vm.sade.organisaatio.resource.YhteystietojenTyyppiResource.createYhteystietojenTyyppi(..))")
    private Object newYhtAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        if (pjp.getArgs() != null && pjp.getArgs()[0] instanceof YhteystietojenTyyppiDTO) {
            YhteystietojenTyyppiDTO yhteystietojenTyyppiDTO = ((YhteystietojenTyyppiDTO) pjp.getArgs()[0]);
            LogMessage logMessage = builder().id(getTekija()).setOperaatio(OrganisaatioOperation.YHTEYSTIETO_CREATE)
                    .oidList(yhteystietojenTyyppiDTO.getOid()).build();
            audit.log(logMessage);
        }
        else {
            LOG.warn("UNKNOWN PARAMETER IN AuditLogAspect newYhtAdvice");
        }
        return result;
    }

    // DELETE /yhteystietojentyyppi/{oid}
    @Around("execution(public * fi.vm.sade.organisaatio.resource.YhteystietojenTyyppiResource.deleteYhteystietottyypi(..))")
    private Object deleteYhtAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        if (pjp.getArgs() != null && pjp.getArgs()[0] instanceof String) {
            String oid = ((String) pjp.getArgs()[0]);
            LogMessage logMessage = builder().id(getTekija()).setOperaatio(OrganisaatioOperation.YHTEYSTIETO_DELETE).oidList(oid).build();
            audit.log(logMessage);
        }
        else {
            LOG.warn("UNKNOWN PARAMETER IN AuditLogAspect deleteYhtAdvice");
        }
        return result;
    }

    // PUT /organisaatio/v2/{oid}/nimet
    @Around("execution(public * fi.vm.sade.organisaatio.resource.impl.v2.OrganisaatioResourceImplV2.newOrganisaatioNimi(..))")
    private Object createOrgNimiAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        if (pjp.getArgs() != null && pjp.getArgs()[0] instanceof String) {
            String oid = ((String) pjp.getArgs()[0]);
            LogMessage logMessage = builder().id(getTekija()).setOperaatio(OrganisaatioOperation.ORG_NIMI_CREATE).oidList(oid).build();
            audit.log(logMessage);
        }
        else {
            LOG.warn("UNKNOWN PARAMETER IN AuditLogAspect createOrgNimiAdvice");
        }
        return result;
    }

    // PUT /organisaatio/v2/muokkaamonta
    @Around("execution(public * fi.vm.sade.organisaatio.resource.impl.v2.OrganisaatioResourceImplV2.muokkaaMontaOrganisaatiota(..))")
    private Object updateOrgManyAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        if (result instanceof OrganisaatioMuokkausTulosListaDTO) {
            OrganisaatioMuokkausTulosListaDTO organisaatioMuokkausTulosListaDTO = (OrganisaatioMuokkausTulosListaDTO) result;
            LogMessage logMessage = builder().id(getTekija()).setOperaatio(OrganisaatioOperation.ORG_UPDATE_MANY)
                    .oidList(organisaatioMuokkausTulosListaDTO.toString()).build();
            audit.log(logMessage);
        }
        else {
            LOG.warn("UNKNOWN PARAMETER IN AuditLogAspect updateOrgManyAdvice");
        }
        return result;
    }

    // POST /organisaatio/v2/{oid}/organisaatiosuhde
    @Around("execution(public * fi.vm.sade.organisaatio.resource.impl.v2.OrganisaatioResourceImplV2.changeOrganisationRelationship(..))")
    private Object updateOrgSuhdeAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        if (pjp.getArgs() != null && pjp.getArgs()[0] instanceof String) {
            String oid = ((String) pjp.getArgs()[0]);
            LogMessage logMessage = builder().id(getTekija()).setOperaatio(OrganisaatioOperation.ORG_SUHDE_UPDATE).oidList(oid).build();
            audit.log(logMessage);
        }
        else {
            LOG.warn("UNKNOWN PARAMETER IN AuditLogAspect updateOrgSuhdeAdvice");
        }
        return result;
    }

    // POST /organisaatio/v2/{oid}/nimet/{date: [0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]}
    @Around("execution(public * fi.vm.sade.organisaatio.resource.impl.v2.OrganisaatioResourceImplV2.updateOrganisaatioNimi(..))")
    private Object updateOrgNimiAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        if (pjp.getArgs() != null && pjp.getArgs()[0] instanceof String) {
            String oid = ((String) pjp.getArgs()[0]);
            LogMessage logMessage = builder().id(getTekija()).setOperaatio(OrganisaatioOperation.ORG_NIMI_UPDATE).oidList(oid).build();
            audit.log(logMessage);
        }
        else {
            LOG.warn("UNKNOWN PARAMETER IN AuditLogAspect updateOrgNimiAdvice");
        }
        return result;
    }

    // DELETE /organisaatio/v2/{oid}/nimet/{date: [0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]}
    @Around("execution(public * fi.vm.sade.organisaatio.resource.impl.v2.OrganisaatioResourceImplV2.deleteOrganisaatioNimi(..))")
    private Object deleteOrgNimiAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        if (pjp.getArgs() != null && pjp.getArgs()[0] instanceof String) {
            String oid = ((String) pjp.getArgs()[0]);
            LogMessage logMessage = builder().id(getTekija()).setOperaatio(OrganisaatioOperation.ORG_NIMI_DELETE).oidList(oid).build();
            audit.log(logMessage);
        }
        else {
            LOG.warn("UNKNOWN PARAMETER IN AuditLogAspect deleteOrgNimiAdvice");
        }
        return result;
    }

    // POST /tempfile/
    @Around("execution(public * fi.vm.sade.organisaatio.resource.TempFileResource.addImage(..))")
    private Object newImgAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        LogMessage logMessage = builder().id(getTekija()).setOperaatio(OrganisaatioOperation.IMG_CREATE).build();
        audit.log(logMessage);
        return result;
    }

    // DELETE /tempfile/{img}
    @Around("execution(public * fi.vm.sade.organisaatio.resource.TempFileResource.deleteImage(..))")
    private Object deleteImgAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        LogMessage logMessage = builder().id(getTekija()).setOperaatio(OrganisaatioOperation.IMG_DELETE).build();
        audit.log(logMessage);
        return result;
    }

    private String getTekija() {
        if (SecurityContextHolder.getContext() != null
                && SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null) {
            return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        } else {
            return null;
        }
    }
}
