/*
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

package fi.vm.sade.organisaatio.service.converter;

import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dao.YhteystietoElementtiDAO;
import java.lang.reflect.ParameterizedType;

import javax.persistence.EntityManager;

import fi.vm.sade.organisaatio.model.OrganisaatioBaseEntity;

/**
 * @author Antti Salonen
 * @param <DTOCLASS>
 * @param <JPACLASS>
 */
public class Converter<DTOCLASS, JPACLASS extends OrganisaatioBaseEntity> {

    protected Class<DTOCLASS> dtoClass;
    protected Class<JPACLASS> jpaClass;
    protected ConverterFactory converterFactory;
    protected EntityManager entityManager;

    public Converter(ConverterFactory converterFactory, EntityManager entityManager) {
        this.converterFactory = converterFactory;
        this.entityManager = entityManager;
        dtoClass = initDtoClass();
        jpaClass = initJpaClass();
    }

    protected Class initDtoClass() {
        return (Class) ((ParameterizedType) (getClass().getGenericSuperclass())).getActualTypeArguments()[0];
    }

    protected Class initJpaClass() {
        return (Class) ((ParameterizedType) (getClass().getGenericSuperclass())).getActualTypeArguments()[1];
    }

    public Class<JPACLASS> getJpaClass() {
        return jpaClass;
    }

    public boolean supportsDtoClass(Class<? extends DTOCLASS> dtoClassParam) {
        return dtoClass.equals(dtoClassParam);
    }

    public boolean supportsEntityClass(Class<? extends JPACLASS> jpaClassParam) {
        return jpaClass.equals(jpaClassParam);
    }

    public void setValuesToDTO(JPACLASS entity, DTOCLASS dto) {

    }

    public void setValuesToJPA(DTOCLASS dto, JPACLASS entity, boolean merge) {

    }

    public void setValuesToJPA(DTOCLASS dto, JPACLASS entity, boolean merge, OrganisaatioDAO organisaatioDAO) {

    }

    public void setValuesToJPA(DTOCLASS dto, JPACLASS entity, boolean merge, OrganisaatioDAO organisaatioDAO, YhteystietoElementtiDAO yteDao) {

    }

}
