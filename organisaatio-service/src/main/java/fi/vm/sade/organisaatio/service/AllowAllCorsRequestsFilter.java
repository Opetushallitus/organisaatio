/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
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
 */
package fi.vm.sade.organisaatio.service;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple filter to ALLOW ALL CORS-requests. At development time it is _really_ annoying to have CORS-related difficulties.
 *
 * @author mlyly
 */
public class AllowAllCorsRequestsFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(AllowAllCorsRequestsFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info("init()");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        LOG.debug("doFilter()");

        if (response instanceof HttpServletResponse) {
            HttpServletRequest req = (HttpServletRequest) request;
            String headerOrigin = req.getHeader("Origin");

            if (headerOrigin != null) {
                LOG.info("  fixing CORS --> allow: '{}'", headerOrigin);

                HttpServletResponse res = (HttpServletResponse) response;
                res.addHeader("Access-Control-Allow-Origin", headerOrigin);
                res.addHeader("Access-Control-Allow-Credentials", "true");
                res.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
                res.addHeader("Access-Control-Allow-Headers", "X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
                res.addHeader("Access-Control-Max-Age", "1728000");
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        LOG.info("desctroy()");
    }

}
