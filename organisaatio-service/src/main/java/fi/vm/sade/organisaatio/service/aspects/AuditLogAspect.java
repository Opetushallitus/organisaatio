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
import fi.vm.sade.organisaatio.resource.dto.ResultRDTO;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.Optional;

/**
 * @author: Tuomas Katva Date: 9.8.2013
 */
@Aspect
public class AuditLogAspect {

    protected static final Logger LOG = LoggerFactory.getLogger(AuditLogAspect.class);

    public static final String serviceName = "organisaatio";
    public Audit audit = new Audit(LOG::info, serviceName, ApplicationType.VIRKAILIJA);

    // POST /organisaatio/{oid}
    @Around("execution(public * fi.vm.sade.organisaatio.resource.OrganisaatioResourceImpl.updateOrganisaatio(..))")
    private Object updateOrgAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result;
        try {
            result = pjp.proceed();
        } catch(Exception e) {
            logEvent(null, OrganisaatioOperation.ORG_UPDATE);
            throw e;
        }
        logEvent(pjp.getArgs()[0], OrganisaatioOperation.ORG_UPDATE);
        return result;
    }

    // DELETE /organisaatio/{oid}
    @Around("execution(public * fi.vm.sade.organisaatio.resource.OrganisaatioResourceImpl.deleteOrganisaatio(..))")
    private Object deleteOrgAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result;
        try {
            result = pjp.proceed();
        } catch(Exception e) {
            logEvent(null, OrganisaatioOperation.ORG_DELETE);
            throw e;
        }
        logEvent(pjp.getArgs()[0], OrganisaatioOperation.ORG_DELETE);
        return result;
    }
    // PUT /organisaatio/
    @Around("execution(public * fi.vm.sade.organisaatio.resource.OrganisaatioResourceImpl.newOrganisaatio(..))")
    private Object newOrgAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result;
        try {
            result = pjp.proceed();
        } catch(Exception e) {
            logEvent(null, OrganisaatioOperation.ORG_CREATE);
            throw e;
        }
        logEvent(result, OrganisaatioOperation.ORG_CREATE);
        return result;
    }
    // POST /yhteystietojentyyppi/
    @Around("execution(public * fi.vm.sade.organisaatio.resource.YhteystietojenTyyppiResource.updateYhteystietoTyyppi(..))")
    private Object updateYhtAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result;
        try {
            result = pjp.proceed();
        } catch(Exception e) {
            logEvent(null, OrganisaatioOperation.YHTEYSTIETO_UPDATE);
            throw e;
        }
        logEvent(pjp.getArgs()[0], OrganisaatioOperation.YHTEYSTIETO_UPDATE);
        return result;
    }
    // PUT /yhteystietojentyyppi/
    @Around("execution(public * fi.vm.sade.organisaatio.resource.YhteystietojenTyyppiResource.createYhteystietojenTyyppi(..))")
    private Object newYhtAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result;
        try {
            result = pjp.proceed();
        } catch(Exception e) {
            logEvent(null, OrganisaatioOperation.YHTEYSTIETO_CREATE);
            throw e;
        }
        logEvent(pjp.getArgs()[0], OrganisaatioOperation.YHTEYSTIETO_CREATE);
        return result;
    }

    // DELETE /yhteystietojentyyppi/{oid}
    @Around("execution(public * fi.vm.sade.organisaatio.resource.YhteystietojenTyyppiResource.deleteYhteystietottyypi(..))")
    private Object deleteYhtAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result;
        try {
            result = pjp.proceed();
        } catch(Exception e) {
            logEvent(null, OrganisaatioOperation.YHTEYSTIETO_DELETE);
            throw e;
        }
        logEvent(pjp.getArgs()[0], OrganisaatioOperation.YHTEYSTIETO_DELETE);
        return result;
    }

    // PUT /organisaatio/v2/{oid}/nimet
    @Around("execution(public * fi.vm.sade.organisaatio.resource.impl.v2.OrganisaatioResourceImplV2.newOrganisaatioNimi(..))")
    private Object createOrgNimiAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result;
        try {
            result = pjp.proceed();
        } catch(Exception e) {
            logEvent(null, OrganisaatioOperation.ORG_NIMI_CREATE);
            throw e;
        }
        logEvent(pjp.getArgs()[0], OrganisaatioOperation.ORG_NIMI_CREATE);
        return result;
    }

    // PUT /organisaatio/v2/muokkaamonta
    @Around("execution(public * fi.vm.sade.organisaatio.resource.impl.v2.OrganisaatioResourceImplV2.muokkaaMontaOrganisaatiota(..))")
    private Object updateOrgManyAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result;
        try {
            result = pjp.proceed();
        } catch(Exception e) {
            logEvent(null, OrganisaatioOperation.ORG_UPDATE_MANY);
            throw e;
        }
        logEvent(result, OrganisaatioOperation.ORG_UPDATE_MANY);
        return result;
    }

    // POST /organisaatio/v2/{oid}/organisaatiosuhde
    @Around("execution(public * fi.vm.sade.organisaatio.resource.impl.v2.OrganisaatioResourceImplV2.changeOrganisationRelationship(..))")
    private Object updateOrgSuhdeAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result;
        try {
            result = pjp.proceed();
        } catch(Exception e) {
            logEvent(null, OrganisaatioOperation.ORG_SUHDE_UPDATE);
            throw e;
        }
        logEvent(pjp.getArgs()[0], OrganisaatioOperation.ORG_SUHDE_UPDATE);
        return result;
    }

    // POST /organisaatio/v2/{oid}/nimet/{date: [0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]}
    @Around("execution(public * fi.vm.sade.organisaatio.resource.impl.v2.OrganisaatioResourceImplV2.updateOrganisaatioNimi(..))")
    private Object updateOrgNimiAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result;
        try {
            result = pjp.proceed();
        } catch(Exception e) {
            logEvent(null, OrganisaatioOperation.ORG_NIMI_UPDATE);
            throw e;
        }
        logEvent(pjp.getArgs()[0], OrganisaatioOperation.ORG_NIMI_UPDATE);
        return result;
    }

    // DELETE /organisaatio/v2/{oid}/nimet/{date: [0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]}
    @Around("execution(public * fi.vm.sade.organisaatio.resource.impl.v2.OrganisaatioResourceImplV2.deleteOrganisaatioNimi(..))")
    private Object deleteOrgNimiAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result;
        try {
            result = pjp.proceed();
        } catch(Exception e) {
            logEvent(null, OrganisaatioOperation.ORG_NIMI_DELETE);
            throw e;
        }
        logEvent(pjp.getArgs()[0], OrganisaatioOperation.ORG_NIMI_DELETE);
        return result;
    }

    // POST /tempfile/
    @Around("execution(public * fi.vm.sade.organisaatio.resource.TempFileResource.addImage(..))")
    private Object newImgAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result;
        try {
            result = pjp.proceed();
        } catch(Exception e) {
            logEvent(null, OrganisaatioOperation.IMG_CREATE);
            throw e;
        }
        logEvent(null, OrganisaatioOperation.IMG_CREATE);
        return result;
    }

    // DELETE /tempfile/{img}
    @Around("execution(public * fi.vm.sade.organisaatio.resource.TempFileResource.deleteImage(..))")
    private Object deleteImgAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object result;
        try {
            result = pjp.proceed();
        } catch(Exception e) {
            logEvent(null, OrganisaatioOperation.IMG_DELETE);
            throw e;
        }
        logEvent(null, OrganisaatioOperation.IMG_DELETE);
        return result;
    }

    // Helper function to handle the logging.
    private void logEvent(Object result, OrganisaatioOperation type) {
        String oid = "";
        if(result == null) {
            oid = null;
        }
        else if (result instanceof String) {
            oid = (String) result;
        }
        else if(result instanceof OrganisaatioMuokkausTulosListaDTO) {
            for(OrganisaatioMuokkausTulosDTO organisaatioMuokkausTulosDTO
                    : ((OrganisaatioMuokkausTulosListaDTO)result).getTulokset()) {
                oid+= organisaatioMuokkausTulosDTO.getOid();
            }
        }
        else if(result instanceof YhteystietojenTyyppiDTO) {
            oid = ((YhteystietojenTyyppiDTO) result).getOid();
        }
        else if(result instanceof ResultRDTO) {
            oid = ((ResultRDTO) result).getOrganisaatio().getOid();
        }
        else {
            oid = null;
            LOG.warn("UNKNOWN PARAMETER IN AuditLogAspect {}", type.toString());
        }

        Target target = new Target.Builder().setField("oid", oid).build();
        Changes changes = new Changes.Builder().build();
        audit.log(getUser(), type, target, changes);
    }

    public static User getUser() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return getUser(((ServletRequestAttributes) requestAttributes).getRequest());
        }
        return new User(getIp(), null, null);
    }

    public static User getUser(HttpServletRequest request) {
        Optional<Oid> oid = getOid(request);
        InetAddress ip = getIp(request);
        String session = getSession(request).orElse(null);
        String userAgent = getUserAgent(request).orElse(null);

        if (oid.isPresent()) {
            return new User(oid.get(), ip, session, userAgent);
        } else {
            return new User(ip, session, userAgent);
        }
    }

    public static Optional<Oid> getOid(HttpServletRequest request) {
        return Optional.ofNullable(request.getUserPrincipal()).map(Principal::getName).flatMap(AuditLogAspect::createOid);
    }

    private static Optional<Oid> createOid(String oid) {
        try {
            return Optional.of(new Oid(oid));
        } catch (GSSException e) {
            return Optional.empty();
        }
    }

    public static InetAddress getIp(HttpServletRequest request) {
        try {
            return InetAddress.getByName(HttpServletRequestUtils.getRemoteAddress(request));
        } catch (UnknownHostException e) {
            return getIp();
        }
    }

    public static InetAddress getIp() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            return InetAddress.getLoopbackAddress();
        }
    }

    public static Optional<String> getSession(HttpServletRequest request) {
        return Optional.ofNullable(request.getSession(false)).map(HttpSession::getId);
    }

    public static Optional<String> getUserAgent(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("User-Agent"));
    }

}
