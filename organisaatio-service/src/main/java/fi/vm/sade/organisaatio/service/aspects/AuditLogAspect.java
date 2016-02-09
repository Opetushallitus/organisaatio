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
        logAuditAdvice(pjp, result, OrganisaatioOperation.ORG_UPDATE);
        return result;
    }

    // DELETE /organisaatio/{oid}
    @Around("execution(public * fi.vm.sade.organisaatio.resource.OrganisaatioResourceImpl.deleteOrganisaatio(..))")
    private Object deleteOrgAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        logAuditAdvice(pjp, result, OrganisaatioOperation.ORG_DELETE);
        return result;
    }

    // PUT /organisaatio/
    @Around("execution(public * fi.vm.sade.organisaatio.resource.OrganisaatioResourceImpl.newOrganisaatio(..))")
    private Object newOrgAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        logAuditAdvice(pjp, result, OrganisaatioOperation.ORG_CREATE);
        return result;
    }
    // POST /yhteystietojentyyppi/
    @Around("execution(public * fi.vm.sade.organisaatio.resource.YhteystietojenTyyppiResource.updateYhteystietoTyyppi(..))")
    private Object updateYhtAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        logAuditAdvice(pjp, result, OrganisaatioOperation.YHTEYSTIETO_UPDATE);
        return result;
    }
    // PUT /yhteystietojentyyppi/
    @Around("execution(public * fi.vm.sade.organisaatio.resource.YhteystietojenTyyppiResource.createYhteystietojenTyyppi(..))")
    private Object newYhtAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        logAuditAdvice(pjp, result, OrganisaatioOperation.YHTEYSTIETO_CREATE);
        return result;
    }

    // DELETE /yhteystietojentyyppi/{oid}
    @Around("execution(public * fi.vm.sade.organisaatio.resource.YhteystietojenTyyppiResource.deleteYhteystietottyypi(..))")
    private Object deleteYhtAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        logAuditAdvice(pjp, result, OrganisaatioOperation.YHTEYSTIETO_DELETE);
        return result;
    }

    // PUT /organisaatio/v2/{oid}/nimet
    @Around("execution(public * fi.vm.sade.organisaatio.resource.impl.v2.OrganisaatioResourceImplV2.newOrganisaatioNimi(..))")
    private Object createOrgNimiAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        logAuditAdvice(pjp, result, OrganisaatioOperation.ORG_NIMI_CREATE);
        return result;
    }

    // PUT /organisaatio/v2/muokkaamonta
    @Around("execution(public * fi.vm.sade.organisaatio.resource.impl.v2.OrganisaatioResourceImplV2.muokkaaMontaOrganisaatiota(..))")
    private Object updateOrgManyAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        logAuditAdvice(pjp, result, OrganisaatioOperation.ORG_UPDATE_MANY);
        return result;
    }

    // POST /organisaatio/v2/{oid}/organisaatiosuhde
    @Around("execution(public * fi.vm.sade.organisaatio.resource.impl.v2.OrganisaatioResourceImplV2.changeOrganisationRelationship(..))")
    private Object updateOrgSuhdeAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        logAuditAdvice(pjp, result, OrganisaatioOperation.ORG_SUHDE_UPDATE);
        return result;
    }

    // POST /organisaatio/v2/{oid}/nimet/{date: [0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]}
    @Around("execution(public * fi.vm.sade.organisaatio.resource.impl.v2.OrganisaatioResourceImplV2.updateOrganisaatioNimi(..))")
    private Object updateOrgNimiAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        logAuditAdvice(pjp, result, OrganisaatioOperation.ORG_NIMI_UPDATE);
        return result;
    }

    // DELETE /organisaatio/v2/{oid}/nimet/{date: [0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]}
    @Around("execution(public * fi.vm.sade.organisaatio.resource.impl.v2.OrganisaatioResourceImplV2.deleteOrganisaatioNimi(..))")
    private Object deleteOrgNimiAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        logAuditAdvice(pjp, result, OrganisaatioOperation.ORG_NIMI_DELETE);
        return result;
    }

    // POST /tempfile/
    @Around("execution(public * fi.vm.sade.organisaatio.resource.TempFileResource.addImage(..))")
    private Object newImgAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        logAuditAdvice(pjp, result, OrganisaatioOperation.IMG_CREATE);
        return result;
    }

    // DELETE /tempfile/{img}
    @Around("execution(public * fi.vm.sade.organisaatio.resource.TempFileResource.deleteImage(..))")
    private Object deleteImgAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        logAuditAdvice(pjp, result, OrganisaatioOperation.IMG_DELETE);
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

    private void logAuditAdvice(JoinPoint pjp, Object result, OrganisaatioOperation operationType) throws Throwable {
        LogMessage logMessage;

        ResultRDTO org = null;
        if (result instanceof ResultRDTO) {
            org = (ResultRDTO) result;
        }
        switch (operationType) {
            case ORG_UPDATE:
                if (org != null) {

                    logMessage = builder().id(getTekija()).setOperaatio(OrganisaatioOperation.ORG_UPDATE).build();
                    audit.log(logMessage);
                }
                break;
//            case OPERATION_TYPE_UPDATE:
//                if (pjp.getArgs() != null && pjp.getArgs()[0] instanceof OrganisaatioDTO && org != null) {
////                    logAuditTapahtuma(constructOrganisaatioTapahtuma(org, OPERATION_TYPE_UPDATE, (OrganisaatioDTO) pjp.getArgs()[0]));
//                } else {
//                    if (org != null) {
////                        logAuditTapahtuma(constructOrganisaatioTapahtuma(org, OPERATION_TYPE_UPDATE));
//                    }
//                }
//                break;
//            case OPERATION_TYPE_DELETE:
////                if (pjp.getArgs() != null && pjp.getArgs()[0] instanceof RemoveByOidType) {
////                    String oid = ((RemoveByOidType) pjp.getArgs()[0]).getOid();
////                    logAuditTapahtuma(constructOrganisaatioTapahtuma(oid, OPERATION_TYPE_UPDATE));
////                } else {
////                    LOG.warn("UNKNOWN PARAMETER IN AuditLogAspect delete");
////                }
//                break;
        }
    }
}
