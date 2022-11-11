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
package fi.vm.sade.organisaatio.service.filters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CsrfHeaderFilterTest {
    private static final String CSRF_HEADER_NAME = "CSRF";
    private final static String CSRF_COOKIE_VALUE = "cookie-value";
    private final static String CSRF_HEADER_OLD_VALUE = "old-value";
    private CsrfHeaderFilter csrfHeaderFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpServletRequest nextRequest;
    private FilterChain filterChain;

    @BeforeEach
    public void setup() {
        csrfHeaderFilter = new CsrfHeaderFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = (request, response) -> nextRequest = (HttpServletRequest)request;
    }

	@Test
	public void requestHasNoCookieAtAll_DontAddHeader() throws IOException, ServletException {
        when(request.getCookies()).thenReturn(null);

        csrfHeaderFilter.doFilter(request, response, filterChain);

        assertNull(nextRequest.getHeader(CSRF_HEADER_NAME));
	}

    @Test
    public void requestHasNoCsrfCookie_DontAddHeader() throws IOException, ServletException {
        when(request.getCookies()).thenReturn(createCookies("irrelevant", "some value"));

        csrfHeaderFilter.doFilter(request, response, filterChain);

        assertNull(nextRequest.getHeader(CSRF_HEADER_NAME));
    }

    @Test
    public void requestHasCsrfCookieAndNoCsrfHeader_AddCsrfHeader() throws IOException, ServletException {
        when(request.getCookies()).thenReturn(createCookies(CSRF_HEADER_NAME, CSRF_COOKIE_VALUE));

        csrfHeaderFilter.doFilter(request, response, filterChain);

        assertEquals(CSRF_COOKIE_VALUE, nextRequest.getHeader(CSRF_HEADER_NAME));
    }

    @Test
    public void requestAlreadyHasCsrfHeader_KeepOldCsrfHeader() throws IOException, ServletException {
        when(request.getCookies()).thenReturn(createCookies(CSRF_HEADER_NAME, CSRF_COOKIE_VALUE));
        when(request.getHeader(CSRF_HEADER_NAME)).thenReturn(CSRF_HEADER_OLD_VALUE);

        csrfHeaderFilter.doFilter(request, response, filterChain);

        assertEquals(CSRF_HEADER_OLD_VALUE, nextRequest.getHeader(CSRF_HEADER_NAME), "Still old value");
    }

    private Cookie[] createCookies(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        Cookie[] cookies = new Cookie[1];
        cookies[0] = cookie;
        return cookies;
    }
}
