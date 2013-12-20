package fi.vm.sade.organisaatio.service.converter;

import java.lang.reflect.ParameterizedType;

import javax.persistence.EntityManager;

import fi.vm.sade.organisaatio.dao.OrganisaatioDAOImpl;
import fi.vm.sade.organisaatio.dao.YhteystietoElementtiDAOImpl;
import fi.vm.sade.organisaatio.model.OrganisaatioBaseEntity;

/**
 * @author Antti Salonen
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

    public void setValuesToJPA(DTOCLASS dto, JPACLASS entity, boolean merge, OrganisaatioDAOImpl organisaatioDAO) {

    }

    public void setValuesToJPA(DTOCLASS dto, JPACLASS entity, boolean merge, OrganisaatioDAOImpl organisaatioDAO, YhteystietoElementtiDAOImpl yteDao) {

    }

}
