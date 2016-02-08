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


import fi.vm.sade.auditlog.ApplicationType;
import fi.vm.sade.auditlog.organisaatio.OrganisaatioOperation;
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
import fi.vm.sade.auditlog.organisaatio.LogMessage;
import fi.vm.sade.auditlog.Audit;

/**
 * @author: Tuomas Katva Date: 9.8.2013
 */
@Aspect
public class AuditLogAspect {

    protected static final Logger LOG = LoggerFactory.getLogger(AuditLogAspect.class);

    public static final String serviceName = "Organisaatio-Service";
//    public static final String TARGET_TYPE = "Organisaatio";
//    public static final int OPERATION_TYPE_INSERT = 1;
//    public static final int OPERATION_TYPE_UPDATE = 2;
//    public static final int OPERATION_TYPE_DELETE = 3;

    public Audit audit = new Audit(serviceName, ApplicationType.VIRKAILIJA);

    private void init() {
    }

    @Around("execution(public * fi.vm.sade.organisaatio.resource.OrganisaatioResourceImpl.updateOrganisaatio(..))")
    private Object updateAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();

        logAuditAdvice(pjp, result, OrganisaatioOperation.ORG_UPDATE);
        return result;
    }

//    @Around("execution(public * fi.vm.sade.organisaatio.service.OrganisaatioServiceImpl.createOrganisaatio(..))")
//    private Object insertAdvice(ProceedingJoinPoint pjp) throws Throwable {
//        init();
//
//        Object result = pjp.proceed();
//        logAuditAdvice(pjp, result, OPERATION_TYPE_INSERT);
//        return result;
//    }
//
//    @Around("execution(public * fi.vm.sade.organisaatio.service.OrganisaatioServiceImpl.removeOrganisaatioByOid(..))")
//    private Object deleteAdvice(ProceedingJoinPoint pjp) throws Throwable {
//        init();
//
//        Object result = pjp.proceed();
//        logAuditAdvice(pjp, result, OPERATION_TYPE_DELETE);
//        return result;
//    }

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
        if (result != null && result instanceof ResultRDTO) {
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
