package fi.vm.sade.organisaatio.java_utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetails;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetailsSource;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class OpintopolkuServiceAuthenticationDetailsSource extends ServiceAuthenticationDetailsSource {
    private final ServiceProperties serviceProperties;

    public OpintopolkuServiceAuthenticationDetailsSource(ServiceProperties serviceProperties) {
        super(serviceProperties);
        this.serviceProperties = serviceProperties;
    }

    @Override
    public ServiceAuthenticationDetails buildDetails(HttpServletRequest request) {
        return new OpintopolkuAuthenticationDetails(request, serviceProperties.getService());
    }

    public static class OpintopolkuAuthenticationDetails extends WebAuthenticationDetails implements ServiceAuthenticationDetails {
        private final String serviceUrl;

        public OpintopolkuAuthenticationDetails(HttpServletRequest request, String serviceUrl) {
            super(request);
            this.serviceUrl = serviceUrl;
        }

        @Override
        public String getServiceUrl() {
            return serviceUrl;
        }
    }
}

