/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.vm.sade.organisaatio.swagger;

import io.swagger.core.filter.SwaggerSpecFilter;
import io.swagger.model.ApiDescription;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

import java.util.List;
import java.util.Map;

/**
 * Specification filter to enable hiding of API parameters.
 * --> http://jakubstas.com/spring-jersey-swagger-fine-tuning-exposed-documentation/
 */
public class AccessHiddenSpecFilter implements SwaggerSpecFilter {

    @Override
    public boolean isPropertyAllowed(Model model, Property property, String s, Map<String, List<String>> map, Map<String, String> map1, Map<String, List<String>> map2) {
        return false;
    }

    @Override
    public boolean isParamAllowed(Parameter param, Operation operation, ApiDescription desc, Map<String, List<String>> arg3, Map<String, String> arg4, Map<String, List<String>> arg5) {
        final String paramAccess = param.getAccess();

        return !paramAccess.equalsIgnoreCase("Some(hidden)");
    }

    @Override
    public boolean isOperationAllowed(Operation operation, ApiDescription apiDescription, Map<String, List<String>> map, Map<String, String> map1, Map<String, List<String>> map2) {
        return false;
    }
}
