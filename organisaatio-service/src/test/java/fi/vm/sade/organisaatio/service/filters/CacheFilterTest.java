package fi.vm.sade.organisaatio.service.filters;

import org.junit.jupiter.api.Test;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


class CacheFilterTest {
    @Test
    void doFilter() throws ServletException, IOException {
        CacheFilter instance = new CacheFilter();
        ServletRequest servletRequest = mock(ServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        instance.doFilter(servletRequest, servletResponse, filterChain);
        verify(servletResponse).setHeader("cache-control", instance.getCacheHeaderValue());
    }

    @Test
    void getCacheHeaderValue() {
        CacheFilter instance = new CacheFilter();
        assertThat(instance.getCacheHeaderValue()).isEqualTo("public, max-age=86400");
    }
}