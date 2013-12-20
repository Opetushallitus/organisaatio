package fi.vm.sade.organisaatio.integrationtest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import fi.vm.sade.organisaatio.api.model.GenericFault;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.EmailDTO;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.model.types.OsoiteDTO;
import fi.vm.sade.organisaatio.api.model.types.OsoiteTyyppi;
import fi.vm.sade.organisaatio.api.model.types.PuhelinNumeroTyyppi;
import fi.vm.sade.organisaatio.api.model.types.PuhelinnumeroDTO;
import fi.vm.sade.organisaatio.api.model.types.WwwDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoDTO;

/**
 * @author Antti
 */
public final class OrganisaatioTstUtils {
    
    public static final OsoiteDTO DEFAULT_KAYNTIOSOITE = createOsoite(OsoiteTyyppi.KAYNTI, "Kayntiosoite 2B", "00550", "Helsinki");// = new OsoiteDTO(OsoiteTyyppi.KAYNTI.value(), "Kayntiosoite 2B", "00550", "Helsinki");
    public static final OsoiteDTO DEFAULT_POSTIOSOITE = createOsoite(OsoiteTyyppi.POSTI, "Postiosoite 1A", "00550", "Helsinki");
    public static final WwwDTO DEFAULT_WWW = createWww("http://test.oph.fi");
    public static final EmailDTO DEFAULT_EMAIL = createEmail("asd@asd.asd");
    public static final PuhelinnumeroDTO DEFAULT_PUHELIN =  createPuhelin(PuhelinNumeroTyyppi.PUHELIN, "123");
    public static final PuhelinnumeroDTO DEFAULT_FAKSI  = createPuhelin(PuhelinNumeroTyyppi.FAKSI, "456");
    public static final List<YhteystietoDTO> DEFAULT_YHTEYSTIEDOT = Arrays.asList(DEFAULT_PUHELIN, DEFAULT_POSTIOSOITE, DEFAULT_KAYNTIOSOITE, DEFAULT_FAKSI, DEFAULT_EMAIL, DEFAULT_WWW);
    public static final String DEFAULT_VOIMASSAOLO_ALKU_FI = "10.05.2030";
    public static final String DEFAULT_VOIMASSAOLO_ALKU_EN = "5/10/2030";

    private OrganisaatioTstUtils() {
        
    }
    
    public static OsoiteDTO createOsoite(OsoiteTyyppi osoiteTyyppi, String osoite, String postinumero, String postitoimipaikka) {// "Kayntiosoite 2B", "00550", "Helsinki") {
        OsoiteDTO oDto = new OsoiteDTO();
        oDto.setOsoiteTyyppi(osoiteTyyppi);
        oDto.setOsoite(osoite);
        oDto.setPostinumero(postinumero);
        oDto.setPostitoimipaikka(postitoimipaikka);
        oDto.setYhteystietoOid("" + System.currentTimeMillis() + "" + Math.random());
        return oDto;
    }
    
    public static PuhelinnumeroDTO createPuhelin(PuhelinNumeroTyyppi tyyppi, String puhNro) {
        PuhelinnumeroDTO puhDto = new PuhelinnumeroDTO();//PuhelinNumeroTyyppi.PUHELIN, "123");
        puhDto.setTyyppi(tyyppi);
        puhDto.setPuhelinnumero(puhNro);
        puhDto.setYhteystietoOid("" + System.currentTimeMillis() + "" + Math.random());
        return puhDto;
    }
    
    public static EmailDTO createEmail(String emailOsoite) {
        EmailDTO emailDto = new EmailDTO();
        emailDto.setEmail(emailOsoite);
        emailDto.setYhteystietoOid("" + System.currentTimeMillis() + "" + Math.random());
        return emailDto;
    }
    
    public static WwwDTO createWww(String wwwOsoite) {
        WwwDTO wwwDto = new WwwDTO();
        wwwDto.setWwwOsoite(wwwOsoite);
        wwwDto.setYhteystietoOid("" + System.currentTimeMillis() + "" + Math.random());
        return wwwDto;
    }
    
    

    public static OrganisaatioDTO createKoulutustoimija(OrganisaatioService organisaatioService, String nimi, String ytunnus, String yhteyshlo, String oid) throws GenericFault {
        return createKoulutustoimija(organisaatioService, buildOrganisaatio(nimi, ytunnus), DEFAULT_YHTEYSTIEDOT, oid);
    }

    public static OrganisaatioDTO buildOrganisaatio(String nimi, String ytunnus) {
        OrganisaatioDTO koulutustoimija = new OrganisaatioDTO();
        MonikielinenTekstiTyyppi nimiT = new MonikielinenTekstiTyyppi();
        Teksti nimifi = new Teksti();
        nimifi.setKieliKoodi("fi");
        nimifi.setValue(nimi);
        nimiT.getTeksti().add(nimifi);
        koulutustoimija.setNimi(nimiT);
        koulutustoimija.setYtunnus(ytunnus);
        // koulutustoimija.setNimiLyhenne(ytunnus);
        koulutustoimija.setOid("" + System.currentTimeMillis() + "" + Math.random());
        return koulutustoimija;
    }

    public static OrganisaatioDTO createKoulutustoimija(OrganisaatioService organisaatioService, OrganisaatioDTO koulutustoimija, List<? extends YhteystietoDTO> yhteystiedot, String oid) throws GenericFault {
        //RootOrganisaatioModelDTO model = new RootOrganisaatioModelDTO(koulutustoimija, yhteystiedot);
        OrganisaatioDTO model = koulutustoimija;
        /**List<OrganisaatioTyyppiDTO> tyypit= Arrays.asList(
                new OrganisaatioTyyppiDTO[]{
                new OrganisaatioTyyppiDTO(OrganisaatioTyyppiDTO.KOULUTUSTOIMIJA_STRING)});*/
        //koulutustoimija.setTyypit(//organisaatioService.findOrganisaatioTyyppis(Arrays.asList(new String[]{OrganisaatioTyyppiDTO.KOULUTUSTOIMIJA_STRING})));
        koulutustoimija.getTyypit().add(OrganisaatioTyyppi.KOULUTUSTOIMIJA);
        koulutustoimija.setOid((oid != null) ? oid : "" + System.currentTimeMillis() + "" + Math.random());
        koulutustoimija.setAlkuPvm(new Date());
        koulutustoimija.setLakkautusPvm(new Date(new Date().getTime()+1000*60*60*24));
        //model.setOrganisaatio(koulutustoimija);
        attachYhteystiedotToOrganisaatioModel(model, yhteystiedot);
        //model.getOrganisaatio().setKieli("Suomi");
        model.getKielet().add("Suomi");
        try {
            OrganisaatioDTO result = organisaatioService.createOrganisaatio(model, false);//createKoulutustoimija((RootOrganisaatioModelDTO) model.clone(true));
            return organisaatioService.findByOid(result.getOid());//read(result.);
        } catch (GenericFault ex) {
            if (ex.getMessage().equals("exception.organisaatio.hierarchy")) {
                fail("Organisaatiohierarchy exception occurred while creating koulutustoimija.");
            } else {
                throw ex;
            }
        }
        return null;
    }

    public static void checkBusinessException(OrganisaatioService organisaatioService) { // TODO: siirrä johonkin utilsiin?
        assertNotNull("service is null", organisaatioService);
       /* try {
            organisaatioService.ping("!businessexception");
            fail("should throw business exception");
        } catch (GenericFault e) {
            //assertEquals("[ping test validation error]", e.getMessage());
            assertNotNull(e.getMessage());
        }*/
    }

    public static String buildYtunnus() {
        return buildYtunnus(System.currentTimeMillis(), 0);
    }

    public static String buildYtunnus(long time, int lastDigit) {
        String ytunnus = "" + time;
        ytunnus = ytunnus.substring(ytunnus.length() - 7, ytunnus.length()) + "-" + lastDigit;
        return ytunnus;
    }

    public static OrganisaatioDTO buildCreateOrganisaatioModel(OrganisaatioService organisaatioService,
                                                                         String nameSub, String parentOid, String organisaatioTyyppi)
            throws GenericFault {

        OrganisaatioDTO model = buildOrganisaatio(nameSub, buildYtunnus());//new OrganisaatioDTO(buildOrganisaatio(nameSub, buildYtunnus()));
        model.getYhteystiedot().add(DEFAULT_POSTIOSOITE);//setPostiosoite(DEFAULT_POSTIOSOITE);
        model.getYhteystiedot().add(DEFAULT_KAYNTIOSOITE);
        model.getYhteystiedot().add(DEFAULT_PUHELIN);
        model.getYhteystiedot().add(DEFAULT_FAKSI);
        model.getYhteystiedot().add(DEFAULT_WWW);
        model.getYhteystiedot().add(DEFAULT_EMAIL);
        if (organisaatioTyyppi != null) {
            //model.getOrganisaatio().setTyypit(Arrays.asList(organisaatioService.findOrganisaatioTyyppi(organisaatioTyyppi)));
            model.getTyypit().add(fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi.fromValue(organisaatioTyyppi));
        }

        if (parentOid != null ) {
            OrganisaatioDTO parent = organisaatioService.findByOid(parentOid);//read(parentId);
            if(parent!=null) {
                model.setParentOid(parent.getOid());
            }
        }

        return model;
    }
    
    private static List<OrganisaatioTyyppi> findTyyppis(List<String> tyyppis) {
        List<OrganisaatioTyyppi> orgTyyppis = new ArrayList<OrganisaatioTyyppi>();
        for (String curTyyppi : tyyppis) {
            orgTyyppis.add(fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi.fromValue(curTyyppi));
        }
        return orgTyyppis;
    }

    public static OrganisaatioDTO createOrganisaatio(OrganisaatioService organisaatioService, boolean expectedSuccess, List<String> rootTyyppi, List<String> parentTyyppi, List<String> tyyppi) throws GenericFault {//ValidationException, OrganisaatioHierarchyException {
        // create parent (maybe)
        String parentOid = null;
        if (parentTyyppi != null) {
            // create root
            OrganisaatioDTO rootModel = buildCreateOrganisaatioModel(organisaatioService, buildYtunnus(), parentOid, null);
            
            
            rootModel.getTyypit().addAll(findTyyppis(rootTyyppi));//getOrganisaatio().setTyypit(organisaatioService.findOrganisaatioTyyppis(rootTyyppi));
            OrganisaatioDTO root = organisaatioService.createOrganisaatio(rootModel, false);
            // create parent
            if (parentTyyppi.equals(rootTyyppi)) {
                parentOid = root.getOid(); // jos roottyyppi on sama kuin parenttyyppi, root kelpaa myös parentiksi
            } else {
                OrganisaatioDTO parentModel = buildCreateOrganisaatioModel(organisaatioService, buildYtunnus(), root.getOid(), null);
                parentModel.getTyypit().addAll(findTyyppis(parentTyyppi));//getOrganisaatio().setTyypit(organisaatioService.findOrganisaatioTyyppis(parentTyyppi));
                OrganisaatioDTO parent = organisaatioService.createOrganisaatio(parentModel, false);
                parentOid = parent.getOid();
            }
        }
        // create the organisaatio
        OrganisaatioDTO model = buildCreateOrganisaatioModel(organisaatioService, buildYtunnus(), parentOid, null);
        for (String curTyyppi : tyyppi) {
          model.getTyypit().add(fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi.fromValue(curTyyppi));  
        }
        //model.getTyypit().addAll(c)//getOrganisaatio().setTyypit(organisaatioService.findOrganisaatioTyyppis(tyyppi));

        // assert
        if (expectedSuccess) {
            return organisaatioService.createOrganisaatio(model, false);
        } else {
            try {
                organisaatioService.createOrganisaatio(model, false);
                fail("should fail");
            } catch (Throwable e) {
                // caught excepted exception
            }
        }

        return null;
    }
    
    private static void attachYhteystiedotToOrganisaatioModel(OrganisaatioDTO model, List<? extends YhteystietoDTO> yhteystiedot) {
        
        for (YhteystietoDTO yhteystietoDTO : yhteystiedot) {
            model.getYhteystiedot().add(yhteystietoDTO);
            /*if (yhteystietoDTO instanceof OsoiteDTO) {
                OsoiteDTO osoite = (OsoiteDTO) yhteystietoDTO;
                //if (OsoiteTyyppi.KAYNTI.value().equals(osoite.getOsoiteTyyppi().value())) {
                    model.getYhteystiedot().add(osoite);//setKayntiosoite(osoite); //kayntiosoite = osoite;
                //} else {
                    //this.postiosoite = osoite;
                  //  model.setPostiosoite(osoite);
                //}
            } else if (yhteystietoDTO instanceof PuhelinnumeroDTO) {
                PuhelinnumeroDTO puhelinOrFaksi = (PuhelinnumeroDTO) yhteystietoDTO;
                if (PuhelinnumeroDTO.TYYPPI_PUHELIN.equals(puhelinOrFaksi.getTyyppi())) {
                    model.setPuhelin(puhelinOrFaksi);//this.puhelin = puhelinOrFaksi;
                } else {
                    model.setFaksi(puhelinOrFaksi);//this.faksi = puhelinOrFaksi;
                }
            } else if (yhteystietoDTO instanceof WwwDTO) {
                model.setWww((WwwDTO) yhteystietoDTO);//this.www = (WwwDTO) yhteystietoDTO;
            } else if (yhteystietoDTO instanceof EmailDTO) {
                model.setEmail((EmailDTO) yhteystietoDTO);//this.email = (EmailDTO) yhteystietoDTO;
            } else {
                throw new IllegalArgumentException("illegal Yhteystiedot: "+yhteystiedot);
            }*/
        }
        
    }
}
