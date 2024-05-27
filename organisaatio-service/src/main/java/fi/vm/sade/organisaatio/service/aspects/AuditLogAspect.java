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


import fi.vm.sade.auditlog.*;
import fi.vm.sade.javautils.http.HttpServletRequestUtils;
import fi.vm.sade.organisaatio.OrganisaatioOperation;
import fi.vm.sade.organisaatio.api.model.types.YhteystietojenTyyppiDTO;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioMuokkausTulosDTO;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioMuokkausTulosListaDTO;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.dto.v4.ResultRDTOV4;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.Optional;

/**
 * @author: Tuomas Katva Date: 9.8.2013
 */
@Component
@Aspect
@Slf4j
public class AuditLogAspect {
    public static final String SERVICE_NAME = "organisaatio";
    public static final Audit audit = new Audit(log::info, SERVICE_NAME, ApplicationType.VIRKAILIJA);

    // POST /organisaatio/<oid>
    @Around("execution(public * fi.vm.sade.organisaatio.resource.OrganisaatioApi.newOrganisaatio(..))")
    private Object newOrgAdvice(ProceedingJoinPoint pjp) throws Throwable {
        return genericAdvice(pjp,OrganisaatioOperation.ORG_CREATE);
    }

    // POST /organisaatio/<oid>
    @Around("execution(public * fi.vm.sade.organisaatio.resource.RekisterointiResource.vardaRekisterointi(..))")
    private Object vardaRekisterointiAdvice(ProceedingJoinPoint pjp) throws Throwable {
        return genericAdvice(pjp,OrganisaatioOperation.ORG_REKISTEROINTI);
    }

    // DELETE /organisaatio/<oid>
    @Around("execution(public * fi.vm.sade.organisaatio.resource.OrganisaatioApi.deleteOrganisaatio(..))")
    private Object deleteOrgAdvice(ProceedingJoinPoint pjp) throws Throwable {
        return genericAdvice(pjp,OrganisaatioOperation.ORG_DELETE);
    }

    // PUT /organisaatio/
    @Around("execution(public * fi.vm.sade.organisaatio.resource.OrganisaatioApi.updateOrganisaatio(..))")
    private Object updateOrgAdvice(ProceedingJoinPoint pjp) throws Throwable {
        return genericAdvice(pjp,OrganisaatioOperation.ORG_UPDATE);
    }

    // DELETE /organisaatio/<oid>
    @Around("execution(public * fi.vm.sade.organisaatio.resource.OrganisaatioApi.deleteOrganisaatio(..))")
    private Object deletergAdvice(ProceedingJoinPoint pjp) throws Throwable {
        return genericAdvice(pjp,OrganisaatioOperation.ORG_DELETE);
    }

    // PUT /<oid>/tarkasta
    @Around("execution(public * fi.vm.sade.organisaatio.resource.OrganisaatioApi.updateTarkastusPvm(..))")
    private Object tarkastaAdvice(ProceedingJoinPoint pjp) throws Throwable {
        return genericAdvice(pjp,OrganisaatioOperation.ORG_TARKASTA);
    }

    // POST /<oid>/nimet
    @Around("execution(public * fi.vm.sade.organisaatio.resource.OrganisaatioApi.newOrganisaatioNimi(..))")
    private Object newNimiAdvice(ProceedingJoinPoint pjp) throws Throwable {
        return genericAdvice(pjp,OrganisaatioOperation.ORG_NIMI_CREATE);
    }

    // PUT /<oid>/nimet
    @Around("execution(public * fi.vm.sade.organisaatio.resource.OrganisaatioApi.updateOrganisaatioNimi(..))")
    private Object updateNimiAdvice(ProceedingJoinPoint pjp) throws Throwable {
        return genericAdvice(pjp,OrganisaatioOperation.ORG_NIMI_UPDATE);
    }

    // DELETE /<oid>/nimet
    @Around("execution(public * fi.vm.sade.organisaatio.resource.OrganisaatioApi.deleteOrganisaatioNimi(..))")
    private Object deleteNimiAdvice(ProceedingJoinPoint pjp) throws Throwable {
        return genericAdvice(pjp,OrganisaatioOperation.ORG_NIMI_DELETE);
    }

    private Object genericAdvice(ProceedingJoinPoint pjp, OrganisaatioOperation operation) throws Throwable {
        Object result;
        try {
            result = pjp.proceed();
        } catch (Exception e) {
            logEvent(e, operation);
            throw e;
        }
        logEvent(pjp.getArgs()[0], operation);
        return result;
    }


    // Helper function to handle the logging.
    private void logEvent(Object result, OrganisaatioOperation type) {
        String oid = "";
        if (result == null) {
            oid = null;
        } else if (result instanceof String) {
            oid = (String) result;
        } else if (result instanceof OrganisaatioMuokkausTulosListaDTO) {
            for (OrganisaatioMuokkausTulosDTO organisaatioMuokkausTulosDTO
                    : ((OrganisaatioMuokkausTulosListaDTO) result).getTulokset()) {
                oid += organisaatioMuokkausTulosDTO.getOid();
            }
        } else if (result instanceof YhteystietojenTyyppiDTO) {
            oid = ((YhteystietojenTyyppiDTO) result).getOid();
        } else if (result instanceof ResultRDTOV4) {
            oid = ((ResultRDTOV4) result).getOrganisaatio().getOid();
        } else if (result instanceof OrganisaatioRDTOV4) {
            oid = ((OrganisaatioRDTOV4) result).getOid();
        } else {
            oid = null;
            log.error("UNKNOWN PARAMETER IN AuditLogAspect {} {}", type, result);
        }

        Target target = new Target.Builder().setField("oid", oid).build();
        Changes changes = new Changes.Builder().build();
        audit.log(getUser(), type, target, changes);
    }

    private void logEvent(Exception e, OrganisaatioOperation type){
        Target target = new Target.Builder().setField("cause",e.getClass().getName()).setField("errorMessage", e.getMessage()).build();
        Changes changes = new Changes.Builder().build();
        audit.log(getUser(), type, target, changes);
    }

    static User getUser() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return getUser(((ServletRequestAttributes) requestAttributes).getRequest());
        }
        return new User(getIp(), null, null);
    }

    static User getUser(HttpServletRequest request) {
        Optional<Oid> oid = getOid(request);
        InetAddress ip = getIp(request);
        String session = getSession(request).orElse(null);
        String userAgent = getUserAgent(request).orElse(null);

        return oid.map(value -> new User(value, ip, session, userAgent)).orElseGet(() -> new User(ip, session, userAgent));
    }

    static Optional<Oid> getOid(HttpServletRequest request) {
        return Optional.ofNullable(request.getUserPrincipal()).map(Principal::getName).flatMap(AuditLogAspect::createOid);
    }

    static Optional<Oid> createOid(String oid) {
        try {
            return Optional.of(new Oid(oid));
        } catch (GSSException e) {
            return Optional.empty();
        }
    }

    static InetAddress getIp(HttpServletRequest request) {
        try {
            return InetAddress.getByName(HttpServletRequestUtils.getRemoteAddress(request));
        } catch (UnknownHostException e) {
            return getIp();
        }
    }

    static InetAddress getIp() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            return InetAddress.getLoopbackAddress();
        }
    }

    static Optional<String> getSession(HttpServletRequest request) {
        return Optional.ofNullable(request.getSession(false)).map(HttpSession::getId);
    }

    static Optional<String> getUserAgent(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("User-Agent"));
    }

}
