package fi.vm.sade.organisaatio.integrationtest;

import java.util.Locale;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.sade.generic.common.ValidationException;
import fi.vm.sade.generic.service.ValidatorFactoryBean;
import fi.vm.sade.organisaatio.api.model.GenericFault;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.EmailDTO;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.Email;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.service.converter.ConverterFactory;

/**
 * @author Antti
 */
@ContextConfiguration(locations = {
        "classpath:spring/test-context.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class ValidationMessagesTest {

    public static final Locale FI = new Locale("fi", "FI");
    @Autowired
    protected OrganisaatioService organisaatioService;
    
    @Autowired
    TestDataCreator dataUtil;
    
    @Autowired
    private ConverterFactory converterFactory;
    
    @Before
    public void setUp() {
        dataUtil.createInitialTestData();
    }

    @Test
    @Ignore
    public void testValidationMessages() throws ValidationException {
        setLocale(FI);

        OrganisaatioDTO organisaatio = new OrganisaatioDTO();
        organisaatio.setNimi(setNimiValue("fi", "x"));
        // organisaatio.setNimiLyhenne("x");
        EmailDTO email = new EmailDTO();
        
        Organisaatio orgEntity = this.converterFactory.convertOrganisaatioToJPA(organisaatio, true);//convertToJPA(organisaatio, Organisaatio.class, true);
        Email emailEntity = this.converterFactory.convertYhteystietoToJPA(email, true);

/*
        // ko kentälle spesifi viesti + custom constraint and validator (MLTextSize)
        Assert.assertEquals("Kent\u00e4n pituuden tulee olla 3 - 100", getValidationMessage(orgEntity, "nimiFi"));

        // yleinen annotaation sidottu viesti parametreineen
        Assert.assertEquals("Kent\u00e4n pituuden tulee olla 3 - 10", getValidationMessage(orgEntity, "nimiLyhenne"));
*/

        // yleinen annotaation sidottu viesti ilman parametreja
        //organisaatio.setYtunnus(null);
        email.setEmail(null);
        Assert.assertEquals("Pakollinen kentt\u00e4", getValidationMessage(emailEntity, "email"));

        // ValidationExceptionin lokalisoitu viesti
        //RootOrganisaatioModelDTO model = new RootOrganisaatioModelDTO();
        OrganisaatioDTO model = new OrganisaatioDTO();
        /*List<OrganisaatioTyyppiDTO> tyypit= Arrays.asList(
                new OrganisaatioTyyppiDTO[]{
                new OrganisaatioTyyppiDTO(OrganisaatioTyyppiDTO.KOULUTUSTOIMIJA_STRING)});*/
        model.getTyypit().add(OrganisaatioTyyppi.KOULUTUSTOIMIJA);//getOrganisaatio().setTyypit(tyypit);
        model.setNimi(setNimiValue("fi", "x"));
        try {
            organisaatioService.createOrganisaatio(model, false);//createKoulutustoimija(model);
            Assert.fail("should throw ValidtionException");
        } catch (GenericFault e) {
            Assert.assertTrue(e.getMessage().contains("validation.exception")); 
            
        }
        
        /*catch (ValidationException e) {
            Assert.assertTrue("wrong Organisaatio.nimiFi validation error: "+e.getMessage(), e.getMessage().contains("Kent\u00e4n pituuden tulee olla 3 - 100"));
        } catch (OrganisaatioHierarchyException ex) {
            Assert.fail("OrganisaatioHierarchyException was thrown while creating organisaatio");
        }*/
    }
    
    private MonikielinenTekstiTyyppi setNimiValue(String lang, String value) {
    	MonikielinenTekstiTyyppi nimiT = new MonikielinenTekstiTyyppi();
    	Teksti nimiTeksti = new Teksti();
    	nimiTeksti.setKieliKoodi(lang);
    	nimiTeksti.setValue(value);
    	nimiT.getTeksti().add(nimiTeksti);
    	return nimiT;
    }

    
    @Test
    //@Ignore
    public void testMultilingualValidationMessages() throws ValidationException {
        EmailDTO email = new EmailDTO();
        email.setEmail(null);
        
        Email emailEntity = this.converterFactory.convertYhteystietoToJPA(email, true);

        setLocale(FI);
        Assert.assertEquals("Pakollinen kentt\u00e4", getValidationMessage(emailEntity, "email"));

        setLocale(Locale.US);
        Assert.assertEquals("Pakollinen kentt\u00e4", getValidationMessage(emailEntity, "email"));
    }

    private void setLocale(Locale locale) {
        Locale.setDefault(locale);
        LocaleContextHolder.setLocale(locale);
    }

    private String getValidationMessage(Object obj, String field) {
        ConstraintViolation<Object> constraintViolation = validate(obj, field);
        return constraintViolation.getMessage();
    }

    private ConstraintViolation<Object> validate(Object obj, String field) {
        Validator validator = ValidatorFactoryBean.getValidator();
        Set<ConstraintViolation<Object>> validationResult = validator.validate(obj);
        for (ConstraintViolation<Object> violation : validationResult) {
            if (violation.getPropertyPath().toString().equals(field)) {
                return violation;
            }
        }
        return null;
    }

}
