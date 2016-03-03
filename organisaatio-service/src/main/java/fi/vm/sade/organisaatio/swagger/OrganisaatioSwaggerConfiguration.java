/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.vm.sade.organisaatio.swagger;

import io.swagger.config.FilterFactory;
import javax.annotation.PostConstruct;

import io.swagger.core.filter.SwaggerSpecFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Configuration bean to set up Swagger.
 * --> http://jakubstas.com/spring-jersey-swagger-fine-tuning-exposed-documentation/
 *
 * @author simok
 */
@Component
public class OrganisaatioSwaggerConfiguration {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        FilterFactory.setFilter(new AccessHiddenSpecFilter());
    }
}