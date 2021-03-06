/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.vm.sade.organisaatio.ytj.service;


import fi.vm.sade.organisaatio.ytj.api.YTJDTO;
import fi.vm.sade.organisaatio.ytj.api.YTJKieli;
import fi.vm.sade.organisaatio.ytj.api.YTJService;
import fi.vm.sade.organisaatio.ytj.api.exception.YtjConnectionException;
import fi.vm.sade.organisaatio.ytj.api.exception.YtjExceptionType;
import fi.ytj.*;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.ws.soap.SOAPFaultException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tuomas Katva
 *
 */
public class YTJServiceImpl implements YTJService {

    private static final Logger LOG = LoggerFactory.getLogger(YTJServiceImpl.class);
    private String asiakastunnus = "";
    private String salainenavain = "";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String HASH_ALGORITHM = "SHA-1";
    private static final String ENCODING = "UTF-8";
    private String aikaleima = "";
    private String tarkiste = "";
    private String tiketti = "";
    private YtjDtoMapperHelper mapper = new YtjDtoMapperHelper();

    public String createHashHex(String strToHash) {
        try {
            byte[] strBytes = strToHash.getBytes(ENCODING);
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] digestBytes = md.digest(strBytes);
               
            return Hex.encodeHexString(digestBytes).toUpperCase();
        } catch (Exception exp) {
            //DEBUGSAWAY:LOG.debug("Exception when creating hashHex : " + exp.toString());
            return null;
        }
    }

    private String createHashString() {
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        aikaleima = df.format(new Date());
        return getAsiakastunnus() + getSalainenavain() + aikaleima;
        
    }

    @Override
    public List<YTJDTO> findByYNimi(String nimi, boolean naytaPassiiviset, YTJKieli kieli) throws YtjConnectionException {


        Kieli kiali = getKieli(kieli);

        YritysTiedot yt = new YritysTiedot();
        YritysTiedotSoap ytj = yt.getYritysTiedotSoap12();
        tarkiste = this.createHashHex(this.createHashString());
        YritysHakutulos vastaus = null;
        try {
            
            vastaus = ytj.wmYritysHaku(nimi,
                    "",
                    false,
                    "",
                    naytaPassiiviset,
                    kiali,
                    getAsiakastunnus(),
                    aikaleima,
                    tarkiste,
                    tiketti);


        } catch (SOAPFaultException exp) {
            LOG.error("SOAPException connecting to YTJ-service : " + exp.getFault().getFaultCode() + " " + exp.getFault().getFaultString(), exp);
            throw new YtjConnectionException(YtjExceptionType.SOAP, exp.getFault().getFaultString());

        } catch (Exception commonExp) {
            LOG.error("Exception occured in YTJ-service : " + commonExp.toString(), commonExp);
            throw new YtjConnectionException(YtjExceptionType.OTHER, commonExp.getMessage());
        }

        if (vastaus == null) {
            LOG.error("Exception in YTJ-service : reply was null");
            throw new YtjConnectionException(YtjExceptionType.OTHER, "Error connecting to service");  
        }
        
        if (vastaus.getYritysHaku() == null) {
          if (vastaus.getVirheTiedot() != null) { 
              LOG.error("Exception occurred when connecting to YTJ-service: " + vastaus.getVirheTiedot().getMessage());
          throw new YtjConnectionException(YtjExceptionType.OTHER, vastaus.getVirheTiedot().getMessage());  
          } else {
              throw new YtjConnectionException(YtjExceptionType.OTHER, "Error connecting to service, vastaus  : " 
                      + " AIKALEIMA :  " + aikaleima 
                      + " tarkiste : " + tarkiste
                      + " tunnistustiedot " + vastaus.getTunnistusTiedot().getTunnistusStatus().value());  
          }
        } 
        List<YritysHakuDTO> ytjObjects =  vastaus.getYritysHaku().getYritysHakuDTO();
        List<YTJDTO> dtos = mapper.mapYritysHakuDTOListToDtoList(ytjObjects);
        return dtos;


    }

    private Kieli getKieli(YTJKieli kieliParam) {
        Kieli selectedLang;
        switch (kieliParam) {
            case EN:
                selectedLang = Kieli.EN;
                break;

            case SV:
                selectedLang = Kieli.SV;
                break;

            default:

                selectedLang = Kieli.FI;
                break;



        }
        return selectedLang;
    }

    @Override
    public YTJDTO findByYTunnus(String ytunnus, YTJKieli kieli) throws YtjConnectionException {


        Kieli kiali = getKieli(kieli);
        YritysTiedot yt = new YritysTiedot();
        YritysTiedotSoap ytj = yt.getYritysTiedotSoap12();
        tarkiste = this.createHashHex(this.createHashString());
        YritysTiedotV2DTO vastaus;
        try {
        vastaus = ytj.wmYritysTiedotV2(ytunnus,
                kiali,
                asiakastunnus,
                aikaleima,
                tarkiste,
                tiketti);

        } catch (SOAPFaultException exp) {
            LOG.error("SOAPFaultException : " + exp.getFault().getFaultString(), exp);
            throw new YtjConnectionException(YtjExceptionType.SOAP, exp.getFault().getFaultString());

        } catch (Exception commonExp) {
            LOG.error("Unknown exception in YTJ-service : " + commonExp, commonExp);
            throw new YtjConnectionException(YtjExceptionType.OTHER, commonExp.getMessage());
        }

        if (vastaus == null) {
            LOG.error("YTJ service returned null reply");
            throw new YtjConnectionException(YtjExceptionType.OTHER, "Error connecting to service");  
        }

        return mapper.mapYritysTiedotV2DTOtoYTJDTO(vastaus);
    }

    // Fetch information of multiple organisations from YTJ by providing list of ytunnuses
    // NOTE: there is a limit of 1000 ytunnuses for one call
    @Override
    public List<YTJDTO> findByYTunnusBatch(List<String> ytunnuses, YTJKieli ytjKieli) throws YtjConnectionException {
        List<YTJDTO> ytjdtos = new ArrayList<YTJDTO>();
        String ytunnusStr = "";
        // ytunnuses need to be separated by semicolon
        for(String str : ytunnuses) {
            ytunnusStr += str + ";";
        }
        if(ytunnusStr.length() > 0 && ytunnusStr.charAt(ytunnusStr.length()-1) == ';') {
            ytunnusStr = ytunnusStr.substring(0, ytunnusStr.length()-1);
        }
        Kieli kieli = getKieli(ytjKieli);
        YritysTiedot yt = new YritysTiedot();
        YritysTiedotSoap ytj = yt.getYritysTiedotSoap12();
        tarkiste = this.createHashHex(this.createHashString());
        ArrayOfYritysTiedotV2DTO vastaus;
        try {
            vastaus = ytj.wmYritysTiedotMassahaku(ytunnusStr,
                    kieli.value(),
                    asiakastunnus,
                    aikaleima,
                    tarkiste,
                    tiketti);

        } catch (SOAPFaultException exp) {
            LOG.error("SOAPFaultException : " + exp.getFault().getFaultString(), exp);
            throw new YtjConnectionException(YtjExceptionType.SOAP, exp.getFault().getFaultString());

        } catch (Exception commonExp) {
            LOG.error("Unknown exception in YTJ-service : " + commonExp, commonExp);
            throw new YtjConnectionException(YtjExceptionType.OTHER, commonExp.getMessage());
        }

        if (vastaus.getYritysTiedotV2DTO().get(0).getVirheTiedot() != null) {
            LOG.error("YTJ service returned null reply");
            throw new YtjConnectionException(YtjExceptionType.OTHER, "Error connecting to service");
        }

        for(YritysTiedotV2DTO yritysTiedotV2DTO : vastaus.getYritysTiedotV2DTO()) {
            ytjdtos.add(mapper.mapYritysTiedotV2DTOtoYTJDTO(yritysTiedotV2DTO));
        }

        return ytjdtos;
    }

    /**
     * @return the asiakastunnus
     */
    public String getAsiakastunnus() {
        return asiakastunnus;
    }

    /**
     * @param asiakastunnus the asiakastunnus to set
     */
    public void setAsiakastunnus(String asiakastunnus) {
        this.asiakastunnus = asiakastunnus;
    }

    /**
     * @return the salainenavain
     */
    public String getSalainenavain() {
        return salainenavain;
    }

    /**
     * @param salainenavain the salainenavain to set
     */
    public void setSalainenavain(String salainenavain) {
        this.salainenavain = salainenavain;
    }
}
