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


import fi.vm.sade.log.client.LoggerHelper;
import fi.vm.sade.log.model.Tapahtuma;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.RemoveByOidType;
import java.util.Date;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author: Tuomas Katva Date: 9.8.2013
 */
@Aspect
public class AuditLogAspect {

    protected static final Logger LOG = LoggerFactory.getLogger(AuditLogAspect.class);

    public static final String SYSTEM = "organisaatio-service";
    public static final String TARGET_TYPE = "Organisaatio";
    public static final int OPERATION_TYPE_INSERT = 1;
    public static final int OPERATION_TYPE_UPDATE = 2;
    public static final int OPERATION_TYPE_DELETE = 3;

    @Autowired(required = true)
    private fi.vm.sade.log.client.Logger auditLogger;

    private void init() {
        LoggerHelper.init(auditLogger);
    }

    @Around("execution(public * fi.vm.sade.organisaatio.service.OrganisaatioServiceImpl.updateOrganisaatio(..))")
    private Object updateAdvice(ProceedingJoinPoint pjp) throws Throwable {
        init();

        Object result = pjp.proceed();
        logAuditAdvice(pjp, result, OPERATION_TYPE_UPDATE);
        return result;
    }

    @Around("execution(public * fi.vm.sade.organisaatio.service.OrganisaatioServiceImpl.createOrganisaatio(..))")
    private Object insertAdvice(ProceedingJoinPoint pjp) throws Throwable {
        init();

        Object result = pjp.proceed();
        logAuditAdvice(pjp, result, OPERATION_TYPE_INSERT);
        return result;
    }

    @Around("execution(public * fi.vm.sade.organisaatio.service.OrganisaatioServiceImpl.removeOrganisaatioByOid(..))")
    private Object deleteAdvice(ProceedingJoinPoint pjp) throws Throwable {
        init();

        Object result = pjp.proceed();
        logAuditAdvice(pjp, result, OPERATION_TYPE_DELETE);
        return result;
    }

    private void logAuditTapahtuma(Tapahtuma tapahtuma) {
        LoggerHelper.log();
    }

    private Tapahtuma constructOrganisaatioTapahtuma(OrganisaatioDTO organisaatio, int tapahtumaTyyppi) {
        // TODO organisation changes are not tracked
        return constructOrganisaatioTapahtuma(organisaatio != null ? organisaatio.getOid() : null, tapahtumaTyyppi);
    }

    private Tapahtuma constructOrganisaatioTapahtuma(String orgOid, int tapahtumaTyyppi) {
        String user = getTekija();

        String target = orgOid;

        Tapahtuma t = LoggerHelper.getAuditRootTapahtuma();

//        t.setHost(user);
        t.setSystem(SYSTEM);
        t.setTarget(target);
        t.setTargetType(TARGET_TYPE);
        t.setTimestamp(new Date());
        t.setType("???");
        t.setUser(user);
        t.setUserActsForUser(null);

        if (tapahtumaTyyppi == OPERATION_TYPE_DELETE) {
            t.setType("DELETE");
        }
        if (tapahtumaTyyppi == OPERATION_TYPE_INSERT) {
            t.setType("INSERT");
        }
        if (tapahtumaTyyppi == OPERATION_TYPE_UPDATE) {
            t.setType("UPDATE");
        }

        return t;
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

    private Tapahtuma constructOrganisaatioTapahtuma(OrganisaatioDTO org, int tapahtumaTyyppi, OrganisaatioDTO vanhaOrg) {
        Tapahtuma t = constructOrganisaatioTapahtuma(org, tapahtumaTyyppi);

        if (vanhaOrg != null) {
            // TODO log field changes
            // t.addValueChange("foo", "oldValue", "newValue");
        }

        return t;
    }

    private void logAuditAdvice(JoinPoint pjp, Object result, int operationType) throws Throwable {

        OrganisaatioDTO org = null;
        if (result instanceof OrganisaatioDTO) {
            org = (OrganisaatioDTO) result;
        }
        switch (operationType) {
            case OPERATION_TYPE_INSERT:
                if (org != null) {
                    logAuditTapahtuma(constructOrganisaatioTapahtuma(org, OPERATION_TYPE_INSERT));
                }
                break;
            case OPERATION_TYPE_UPDATE:
                if (pjp.getArgs() != null && pjp.getArgs()[0] instanceof OrganisaatioDTO && org != null) {
                    logAuditTapahtuma(constructOrganisaatioTapahtuma(org, OPERATION_TYPE_UPDATE, (OrganisaatioDTO) pjp.getArgs()[0]));
                } else {
                    if (org != null) {
                        logAuditTapahtuma(constructOrganisaatioTapahtuma(org, OPERATION_TYPE_UPDATE));
                    }
                }
                break;
            case OPERATION_TYPE_DELETE:
                if (pjp.getArgs() != null && pjp.getArgs()[0] instanceof RemoveByOidType) {
                    String oid = ((RemoveByOidType) pjp.getArgs()[0]).getOid();
                    logAuditTapahtuma(constructOrganisaatioTapahtuma(oid, OPERATION_TYPE_UPDATE));
                } else {
                    LOG.warn("UNKNOWN PARAMETER IN AuditLogAspect delete");
                }
                break;
        }
    }
}
