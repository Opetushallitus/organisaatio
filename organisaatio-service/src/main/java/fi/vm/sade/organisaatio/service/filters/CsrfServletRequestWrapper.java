package fi.vm.sade.organisaatio.service.filters;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.*;

public class CsrfServletRequestWrapper extends HttpServletRequestWrapper {
    private Map<String, String> headerMap;

    public CsrfServletRequestWrapper(HttpServletRequest request) {
        super(request);
        headerMap = new HashMap<String, String>();
    }

    @Override
    public String getHeader(String name) {
        String headerValue = super.getHeader(name);
        if (headerMap.containsKey(name)) {
            headerValue = headerMap.get(name);
        }

        return headerValue;
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> headers = Collections.list(super.getHeaders(name));
        if (headerMap.containsKey(name)) {
            headers.add(headerMap.get(name));
        }
        return Collections.enumeration(headers);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> headerNames = Collections.list(super.getHeaderNames());
        headerNames.addAll(headerMap.keySet());
        return Collections.enumeration(headerNames);
    }

    public void addHeader(String headerName, String headerValue) {
        headerMap.put(headerName, headerValue);
    }
}
