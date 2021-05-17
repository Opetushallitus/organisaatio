package fi.vm.sade.organisaatio.ytj.service;

import fi.vm.sade.organisaatio.ytj.api.YTJDTO;
import fi.vm.sade.organisaatio.ytj.api.YTJKieli;
import fi.vm.sade.organisaatio.ytj.api.YTJService;
import fi.vm.sade.organisaatio.ytj.api.exception.YtjConnectionException;
import fi.vm.sade.organisaatio.ytj.api.exception.YtjExceptionType;
import fi.ytj.ArrayOfYritysTiedotV2DTO;
import fi.ytj.Kieli;
import fi.ytj.YritysHakuDTO;
import fi.ytj.YritysHakutulos;
import fi.ytj.YritysTiedot;
import fi.ytj.YritysTiedotSoap;
import fi.ytj.YritysTiedotV2DTO;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.ws.soap.SOAPFaultException;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class YTJServiceImpl implements YTJService {

    private static final Logger LOG = LoggerFactory.getLogger(YTJServiceImpl.class);
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String HASH_ALGORITHM = "SHA-1";
    private static final String ENCODING = "UTF-8";

    private final String tiketti = ""; // ???
    private final YtjDtoMapperHelper mapper = new YtjDtoMapperHelper();
    private final String asiakastunnus;
    private final String salainenavain;

    public YTJServiceImpl(String asiakastunnus, String salainenavain) {
        this.asiakastunnus = asiakastunnus;
        this.salainenavain = salainenavain;
    }

    YTJServiceImpl() {
        this("", ""); // testeille
    }

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

    private String createHashString(String aikaleima) {
        return asiakastunnus + salainenavain + aikaleima;
    }

    private String aikaleima() {
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        return df.format(new Date());
    }

    @Override
    public List<YTJDTO> findByYNimi(String nimi, boolean naytaPassiiviset, YTJKieli kieli) throws YtjConnectionException {
        YritysTiedot yt = new YritysTiedot();
        YritysTiedotSoap ytj = yt.getYritysTiedotSoap12();
        String aikaleima = aikaleima();
        String tarkiste = this.createHashHex(this.createHashString(aikaleima));
        YritysHakutulos vastaus;
        try {
            vastaus = ytj.wmYritysHaku(nimi,
                    "",
                    false,
                    "",
                    naytaPassiiviset,
                    getKieli(kieli),
                    asiakastunnus,
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
        return mapper.mapYritysHakuDTOListToDtoList(ytjObjects);
    }

    private Kieli getKieli(YTJKieli kieliParam) {
        switch (kieliParam) {
            case EN:
                return Kieli.EN;
            case SV:
                return Kieli.SV;
            default:
                return Kieli.FI;
        }
    }

    @Override
    public YTJDTO findByYTunnus(String ytunnus, YTJKieli kieli) throws YtjConnectionException {
        YritysTiedot yt = new YritysTiedot();
        YritysTiedotSoap ytj = yt.getYritysTiedotSoap12();
        String aikaleima = aikaleima();
        String tarkiste = createHashHex(createHashString(aikaleima));
        YritysTiedotV2DTO vastaus;
        try {
            vastaus = ytj.wmYritysTiedotV2(ytunnus,
                    getKieli(kieli),
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
        String ytunnusStr = String.join(";", ytunnuses);
        Kieli kieli = getKieli(ytjKieli);
        YritysTiedot yt = new YritysTiedot();
        YritysTiedotSoap ytj = yt.getYritysTiedotSoap12();
        String aikaleima = aikaleima();
        String tarkiste = createHashHex(createHashString(aikaleima));
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

        return vastaus.getYritysTiedotV2DTO()
                .stream()
                .map(mapper::mapYritysTiedotV2DTOtoYTJDTO)
                .collect(Collectors.toList());
    }

}
