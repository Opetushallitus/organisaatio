package fi.vm.sade.organisaatio.service.converter;

import fi.vm.sade.organisaatio.api.model.types.*;
import fi.vm.sade.organisaatio.dao.*;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import org.dozer.DozerBeanMapper;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.Response;
import java.lang.reflect.Modifier;
import java.util.*;

public class ConverterFactory {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());
    // Entity classes should not be aware of their DTOs (and there might/should be many per entity)
    // Refactored OrganisaatioBaseEntity.getDTOClass():
    private static final Map<Class<?>, Class<?>> DTO_CLASSES_BY_ENTITY = new HashMap<Class<?>, Class<?>>() {{
        put(Yhteystieto.class, YhteystietoDTO.class);
        put(Osoite.class, OsoiteDTO.class);
        put(Email.class, EmailDTO.class);
        put(Puhelinnumero.class, PuhelinnumeroDTO.class);
        put(Www.class, WwwDTO.class);
        put(YhteystietoArvo.class, YhteystietoArvoDTO.class);
        put(YhteystietoElementti.class, YhteystietoElementtiDTO.class);
        put(YhteystietojenTyyppi.class, YhteystietojenTyyppiDTO.class);
    }};
    protected static<T extends OrganisaatioBaseEntity> Class<?> getDtoClass(T entity) {
        return DTO_CLASSES_BY_ENTITY.get(Hibernate.getClass(entity));
    }


    @Autowired
    private DozerBeanMapper mapper;
    @PersistenceContext
    private EntityManager entityManager;
    private List<Converter> converters = new ArrayList<>();

    @Autowired
    private OrganisaatioDAO organisaatioDAO;

    @Autowired
    private YhteystietoArvoDAO yhteystietoArvoDAO;

    @Autowired
    private YhteystietoDAO yhteystietoDAO;

    @Autowired
    private YhteystietoElementtiDAO yhteystietoElementtiDAO;

    @Autowired
    private YhteystietojenTyyppiDAO yhteystietojenTyyppiDAO;
    
    @PostConstruct
    public void initConverters() {
        //registerConverter(new OrganisaatiotyypinYhteystiedotConverter(this, entityManager));
        registerConverter(new YhteystietoArvoConverter(this, entityManager));
        registerConverter(new YhteystietojenTyyppiConverter(this, entityManager));
    }

    public void registerConverter(Converter converter) {
        converters.add(converter);
    }

    public <DTO> Set<DTO> convertToDTO(Set<? extends OrganisaatioBaseEntity> entities, Class<? extends DTO> resultClass) {
        Set<DTO> dtos = new HashSet<>();
        for (OrganisaatioBaseEntity entity : entities) {
            dtos.add(convertToDTO(entity, resultClass));
        }
        return dtos;
    }

    public <DTO> DTO convertToDTO(OrganisaatioBaseEntity entity) {
        return (DTO) convertToDTO(entity, getDtoClass(entity));
    }

    public <DTO> DTO convertToDTO(OrganisaatioBaseEntity entity, Class<? extends DTO> resultClass) {
        DTO dto = null;

        // if resultClass is abstractclass, get resultclass from entity, but ensure it is resultclass' subclass
        if (Modifier.isAbstract(resultClass.getModifiers())) {
            Class temp = getDtoClass(entity);
            if (!resultClass.isAssignableFrom(temp)) {
                throw new IllegalArgumentException("cannot convert, resultClass is abstract and not not assignable from entity's dtoclass, resultClass: " +
                        resultClass + ", entity.dtoclass: " + getDtoClass(entity));
            }
            resultClass = temp;
        }

        // create object and convert basic fields with dozer
        if (entity != null) {
            dto = mapper.map(entity, resultClass);
        }

        // convert other fields with custom converter
        Converter converter = getConverterForDto(resultClass);
        if (converter != null) {
            converter.setValuesToDTO(entity, dto);
        }

        if (entity instanceof Puhelinnumero) {
            ((PuhelinnumeroDTO)dto).setTyyppi(PuhelinNumeroTyyppi.fromValue(((Puhelinnumero) entity).getTyyppi()));
        } else if (entity instanceof Osoite) {
            ((OsoiteDTO)dto).setOsoiteTyyppi(OsoiteTyyppi.fromValue(((Osoite) entity).getOsoiteTyyppi()));
        } else if (entity instanceof YhteystietoElementti) {
            ((YhteystietoElementtiDTO)dto).setTyyppi(YhteystietoElementtiTyyppi.fromValue(((YhteystietoElementti) entity).getTyyppi()));
        }

        //DEBUGSAWAY:log.debug("convertToDTO: " + entity + " -> " + dto);
        return dto;
    }



    private Converter getConverterForDto(Class dtoClass) {
        for (Converter converter : converters){
            if (converter.supportsDtoClass(dtoClass)) {
                return converter;
            }
        }
        return null;
    }

    private Converter getConverterForEntity(OrganisaatioBaseEntity entity) {
        for (Converter converter : converters){
            if (converter.supportsEntityClass(entity.getClass())) {
                return converter;
            }
        }
        return null;
    }

    private Class getJPAClass(Object dto) {
        Converter converter = getConverterForDto(dto.getClass());
        if (converter != null) {
            return converter.getJpaClass();
        }

        // TODO: omat convertterit näillekin?
        Class<? extends OrganisaatioBaseEntity> jpaClass;
        if (OsoiteDTO.class.isAssignableFrom(dto.getClass())) {
            jpaClass = Osoite.class;
        } else if (PuhelinnumeroDTO.class.isAssignableFrom(dto.getClass())) {
            jpaClass = Puhelinnumero.class;
        } else if (WwwDTO.class.isAssignableFrom(dto.getClass())) {
            jpaClass = Www.class;
        } else if (EmailDTO.class.isAssignableFrom(dto.getClass())) {
            jpaClass = Email.class;
        } //else if (OrganisaatioTyyppiDTO.class.isAssignableFrom(dto.getClass())) {
            //jpaClass = OrganisaatioTyyppi.class;}
        else if (YhteystietoElementtiDTO.class.isAssignableFrom(dto.getClass())) {
            jpaClass = YhteystietoElementti.class;
        } else {
            throw new IllegalArgumentException("no converter found for dto: " + dto);
        }

        return jpaClass;
    }
    
    /**
     * converts dto to jpa entity
     * @param dto
     * @param merge Applicable only for objects that already exist in db (has id). If true, merge changes, otherhwise will reload from db instead converting.
     * @return
     */
    public YhteystietoArvo convertYhteystietoArvoToJPA(YhteystietoArvoDTO dto, boolean merge) {
        YhteystietoArvo entity = null;
        if (dto != null) {

            Class jpaClass = YhteystietoArvo.class;
            // reload if !merge and entity exists in db already
            if (dto.getYhteystietoArvoOid() != null && this.yhteystietoArvoDAO.findBy("yhteystietoArvoOid", dto.getYhteystietoArvoOid()).size() > 0 && !merge) {
                entity = this.yhteystietoArvoDAO.findBy("yhteystietoArvoOid", dto.getYhteystietoArvoOid()).get(0);//(YhteystietoArvo) entityManager.find(jpaClass, dto.getYhteystietoArvoOid());
                //DEBUGSAWAY:log.debug("convertToJPA reloaded object: "+entity);
            } else if (dto.getYhteystietoArvoOid() != null && this.yhteystietoArvoDAO.findBy("yhteystietoArvoOid", dto.getYhteystietoArvoOid()).size() > 0 && merge) {
                // hibernate merge tms jos on kannassa jo ja merge=true, muuten syntyy duplikaatti objekti
                /*
                entity = (JPACLASS) mapper.map(dto, jpaClass);
                entity = entityManager.merge(entity);
                */
                entity = this.yhteystietoArvoDAO.findBy("yhteystietoArvoOid", dto.getYhteystietoArvoOid()).get(0);
                mapper.map(dto, entity);
            } else {
                // or convert fields from dto
                entity = (YhteystietoArvo) mapper.map(dto, jpaClass);
            }
            // organisaatio parent

            Converter converter = getConverterForDto(dto.getClass());
            if (converter != null) {
                converter.setValuesToJPA(dto, entity, merge, this.organisaatioDAO, this.yhteystietoElementtiDAO);
            }

        }
        //DEBUGSAWAY:log.debug("convertToJPA: " + dto + " -> " + entity);
        return entity;
    }

    /**
     * Converts dto to jpa entity
     *
     * @param dto
     * @param merge Applicable only for objects that already exist in db (has id). If true, merge changes, otherhwise will reload from db instead converting.
     * @return
     */
    public YhteystietoElementti convertYhteystietoElementtiToJPA(YhteystietoElementtiDTO dto, boolean merge) {
        YhteystietoElementti entity = null;
        if (dto != null) {

            Class jpaClass = YhteystietoElementti.class;
            // reload if !merge and entity exists in db already
            if (dto.getOid() != null && this.yhteystietoElementtiDAO.findBy("oid", dto.getOid()).size() > 0 && !merge) {
                entity = this.yhteystietoElementtiDAO.findBy("oid", dto.getOid()).get(0);//(YhteystietoElementti) entityManager.find(jpaClass, dto.getNimi());
                //DEBUGSAWAY:log.debug("convertToJPA reloaded object: "+entity);
            } else if (dto.getOid() != null && this.yhteystietoElementtiDAO.findBy("oid", dto.getOid()).size() > 0 && merge) {
                // hibernate merge tms jos on kannassa jo ja merge=true, muuten syntyy duplikaatti objekti
                /*
                entity = (JPACLASS) mapper.map(dto, jpaClass);
                entity = entityManager.merge(entity);
                */
                entity = this.yhteystietoElementtiDAO.findBy("oid", dto.getOid()).get(0);//(YhteystietoElementti) entityManager.find(jpaClass, dto.getNimi());
                mapper.map(dto, entity);
                entity.setTyyppi(dto.getTyyppi().value());

                entity.setPakollinen(dto.isPakollinen());

                entity.setKaytossa(dto.isKaytossa());
            } else {
                // or convert fields from dto

                entity = (YhteystietoElementti) mapper.map(dto, jpaClass);
                entity.setTyyppi(dto.getTyyppi().value());

                entity.setPakollinen(dto.isPakollinen());

                entity.setKaytossa(dto.isKaytossa());
            }
            // organisaatio parent

            Converter converter = getConverterForDto(dto.getClass());
            if (converter != null) {
                converter.setValuesToJPA(dto, entity, merge);
            }

        }
        //DEBUGSAWAY:log.debug("convertToJPA: " + dto + " -> " + entity);
        return entity;
    }

    /**
     * converts dto to jpa entity
     * @param <JPACLASS>
     * @param dto
     * @param merge Applicable only for objects that already exist in db (has id). If true, merge changes, otherhwise will reload from db instead converting.
     * @return
     */
    public <JPACLASS extends Yhteystieto> JPACLASS convertYhteystietoToJPA(YhteystietoDTO dto, boolean merge) {
        JPACLASS entity = null;
        Class jpaClass = getJPAClass(dto);
        if (dto != null) {
            // reload if !merge and entity exists in db already
           if (dto.getYhteystietoOid() != null && yhteystietoDAO.findBy("yhteystietoOid", dto.getYhteystietoOid()).size() > 0 && !merge) {
                entity = (JPACLASS)(yhteystietoDAO.findBy("yhteystietoOid", dto.getYhteystietoOid()).get(0));
                //DEBUGSAWAY://log.debug("convertToJPA reloaded object: "+entity);
            } else if (dto.getYhteystietoOid() != null && yhteystietoDAO.findBy("yhteystietoOid", dto.getYhteystietoOid()).size() > 0 && merge) {
                // hibernate merge tms jos on kannassa jo ja merge=true, muuten syntyy duplikaatti objekti
                /*
                entity = (JPACLASS) mapper.map(dto, jpaClass);
                entity = entityManager.merge(entity);
                */
                entity = (JPACLASS)(yhteystietoDAO.findBy("yhteystietoOid", dto.getYhteystietoOid()).get(0));
                mapper.map(dto, entity);
            } else {
                // or convert fields from dto
                entity = (JPACLASS) mapper.map(dto, jpaClass);
            }
            // organisaatio parent

            if (entity instanceof Puhelinnumero) {
                ((Puhelinnumero) entity).setTyyppi(((PuhelinnumeroDTO)dto).getTyyppi().value());
            } else if (entity instanceof Osoite) {
                ((Osoite) entity).setOsoiteTyyppi(((OsoiteDTO)dto).getOsoiteTyyppi().value());
            }


        }
        //DEBUGSAWAY:log.debug("convertToJPA: " + dto + " -> " + entity);
        return entity;
    }

    public YhteystietojenTyyppi convertYhteystietojenTyyppiToJPA(YhteystietojenTyyppiDTO dto, boolean merge) {
        YhteystietojenTyyppi entity = null;
        Class<YhteystietojenTyyppi> jpaClass = YhteystietojenTyyppi.class;
         if (dto != null) {
             // reload if !merge and entity exists in db already
            if (dto.getOid() != null && this.yhteystietojenTyyppiDAO.findBy("oid", dto.getOid()).size() > 0 && !merge) {
                 entity = this.yhteystietojenTyyppiDAO.findBy("oid", dto.getOid()).get(0);//(YhteystietojenTyyppi) entityManager.find(jpaClass, dto.getOid());
                 //DEBUGSAWAY://log.debug("convertToJPA reloaded object: "+entity);
             } else if (dto.getOid() != null && this.yhteystietojenTyyppiDAO.findBy("oid", dto.getOid()).size() > 0 && merge) {
                 // hibernate merge tms jos on kannassa jo ja merge=true, muuten syntyy duplikaatti objekti
                 /*
                 entity = (JPACLASS) mapper.map(dto, jpaClass);
                 entity = entityManager.merge(entity);
                 */
                 entity = this.yhteystietojenTyyppiDAO.findBy("oid", dto.getOid()).get(0);
                 if (entity.getVersion() != dto.getVersion()) {
                     throw new OrganisaatioResourceException(Response.Status.CONFLICT, "Data version changed.", "yhteystietojentyyppi.exception.modified");
                 }
                 mapper.map(dto, entity);
             } else {
                 // or convert fields from dto

                 entity = mapper.map(dto, jpaClass);

             }
             // organisaatio parent

             Converter converter = getConverterForDto(dto.getClass());
             if (converter != null) {
                 converter.setValuesToJPA(dto, entity, merge);
             }
         }
         //DEBUGSAWAY:log.debug("convertToJPA: " + dto + " -> " + entity);
         return entity;
     }

    public <JPACLASS extends OrganisaatioBaseEntity> List<JPACLASS> convertYhteystiedotToJPA(List<? extends YhteystietoDTO> dtos, Class<? extends JPACLASS> resultClass, boolean merge ) {
        List jpas = new ArrayList();

        if (dtos != null) {
            for (YhteystietoDTO dto : dtos) {
                jpas.add(convertYhteystietoToJPA(dto, merge));
            }
        }
        return jpas;
    }

    public <JPACLASS extends OrganisaatioBaseEntity> List<JPACLASS> convertYhteystietoArvosToJPA(List<YhteystietoArvoDTO> dtos, Class<? extends JPACLASS> resultClass, boolean merge ) {
        List jpas = new ArrayList();
        //YhteystietoConverter ytConv = new YhteystietoConverter(this, entityManager);
        if (dtos != null) {
            for (YhteystietoArvoDTO dto : dtos) {
                if (dto.getArvo() != null
                        && (((dto.getArvo() instanceof String)
                                && ((String)dto.getArvo()).length() > 0
                            || isValidYhteystieto(dto.getArvo())))) {
                    YhteystietoArvo jpa = convertYhteystietoArvoToJPA(dto, merge);
                    if (jpa.getYhteystietoArvoOid() == null) {
                    	jpa.setYhteystietoArvoOid(dto.getYhteystietoArvoOid());
                    }
                    jpas.add(jpa);
                }
            }
        }
        return jpas;
    }

    private boolean isValidYhteystieto(Object yhteystieto) {
        boolean isValid= false;
        if (yhteystieto instanceof OsoiteDTO) {
            OsoiteDTO yhteystietoO = (OsoiteDTO)yhteystieto;
            return yhteystietoO.getPostinumero() != null
                    && yhteystietoO.getOsoite() != null
                    && yhteystietoO.getOsoite().length() > 0
                    && yhteystietoO.getPostitoimipaikka() != null;
        }
        if (yhteystieto instanceof PuhelinnumeroDTO) {
            PuhelinnumeroDTO yhteystietoP = (PuhelinnumeroDTO)yhteystieto;
            return yhteystietoP.getPuhelinnumero() != null && yhteystietoP.getPuhelinnumero().length() > 0;
        }
        if (yhteystieto instanceof EmailDTO) {
            EmailDTO yhteystietoE = (EmailDTO)yhteystieto;
            return yhteystietoE.getEmail() != null && yhteystietoE.getEmail().length() > 0;
        }
        if (yhteystieto instanceof WwwDTO) {
            WwwDTO yhteystietoW = (WwwDTO)yhteystieto;
            return yhteystietoW.getWwwOsoite() != null && yhteystietoW.getWwwOsoite().length() > 0;
        }
        return isValid;
    }

    public Set<YhteystietoElementti> convertYhteystietoElementtisToJPA(Set<YhteystietoElementtiDTO> dtos, boolean merge) {
        Set<YhteystietoElementti> jpas = new HashSet<>();
        if (dtos != null) {
            for (YhteystietoElementtiDTO dto : dtos) {
                YhteystietoElementti jpa = convertYhteystietoElementtiToJPA(dto, merge);
                jpas.add(jpa);
            }
        }
        return jpas;
    }

}
