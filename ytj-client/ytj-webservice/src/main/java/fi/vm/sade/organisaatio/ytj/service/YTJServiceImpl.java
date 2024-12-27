package fi.vm.sade.organisaatio.ytj.service;

import fi.vm.sade.organisaatio.ytj.api.YTJDTO;
import fi.vm.sade.organisaatio.ytj.api.YTJKieli;
import fi.vm.sade.organisaatio.ytj.api.YTJService;
import fi.vm.sade.organisaatio.ytj.api.exception.YtjConnectionException;
import fi.vm.sade.organisaatio.ytj.api.exception.YtjExceptionType;
import fi.ytj.*;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.soap.SOAPFaultException;
import java.nio.charset.StandardCharsets;
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
    private static final String DEFAULT_ERROR_MESSAGE = "Error connecting to service";
    private static final String TIKETTI = ""; // ???
    private final YtjDtoMapperHelper mapper = new YtjDtoMapperHelper();
    private final String asiakastunnus;
    private final String salainenavain;
    private final String ytjUrl;

    public YTJServiceImpl(String asiakastunnus, String salainenavain, String ytjUrl) {
        this.asiakastunnus = asiakastunnus;
        this.salainenavain = salainenavain;
        this.ytjUrl = ytjUrl;
    }


    public String createHashHex(String strToHash) {
        try {
            byte[] strBytes = strToHash.getBytes(StandardCharsets.UTF_8);
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] digestBytes = md.digest(strBytes);

            return Hex.encodeHexString(digestBytes).toUpperCase();
        } catch (Exception exp) {
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
        YritysTiedotSoap ytj = getYritysTiedotSoap();
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
                    TIKETTI);
        } catch (SOAPFaultException exp) {
            throw new YtjConnectionException(YtjExceptionType.SOAP, exp.getFault().getFaultString());

        } catch (Exception commonExp) {
            throw new YtjConnectionException(YtjExceptionType.OTHER, commonExp.getMessage());
        }

        if (vastaus == null) {
            throw new YtjConnectionException(YtjExceptionType.OTHER, DEFAULT_ERROR_MESSAGE);
        }

        if (vastaus.getYritysHaku() == null) {
            if (vastaus.getVirheTiedot() != null) {
                throw new YtjConnectionException(YtjExceptionType.OTHER, vastaus.getVirheTiedot().getMessage());
            } else {
                throw new YtjConnectionException(YtjExceptionType.OTHER, "Error connecting to service, vastaus  : "
                        + " AIKALEIMA :  " + aikaleima
                        + " tarkiste : " + tarkiste
                        + " tunnistustiedot " + vastaus.getTunnistusTiedot().getTunnistusStatus().value());
            }
        }
        List<YritysHakuDTO> ytjObjects = vastaus.getYritysHaku().getYritysHakuDTO();
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
        YritysTiedotSoap ytj = getYritysTiedotSoap();
        String aikaleima = aikaleima();
        String tarkiste = createHashHex(createHashString(aikaleima));
        YritysTiedotV2DTO vastaus;
        try {
            vastaus = ytj.wmYritysTiedotV2(ytunnus,
                    getKieli(kieli),
                    asiakastunnus,
                    aikaleima,
                    tarkiste,
                    TIKETTI);
        } catch (SOAPFaultException exp) {
            throw new YtjConnectionException(YtjExceptionType.SOAP, exp.getFault().getFaultString());
        } catch (Exception commonExp) {
            throw new YtjConnectionException(YtjExceptionType.OTHER, commonExp.getMessage());
        }
        if (vastaus == null) {
            LOG.error("YTJ service returned null reply for {}", ytunnus);
            throw new YtjConnectionException(YtjExceptionType.OTHER, DEFAULT_ERROR_MESSAGE);
        }
        return mapper.mapYritysTiedotV2DTOtoYTJDTO(vastaus);
    }

    // Fetch information of multiple organisations from YTJ by providing list of ytunnuses
    // NOTE: there is a limit of 1000 ytunnuses for one call
    @Override
    public List<YTJDTO> findByYTunnusBatch(List<String> ytunnuses, YTJKieli ytjKieli) throws YtjConnectionException {
        String ytunnusStr = String.join(";", ytunnuses);
        Kieli kieli = getKieli(ytjKieli);
        YritysTiedotSoap ytj = getYritysTiedotSoap();
        String aikaleima = aikaleima();
        String tarkiste = createHashHex(createHashString(aikaleima));
        ArrayOfYritysTiedotV2DTO vastaus;
        try {
            vastaus = ytj.wmYritysTiedotMassahaku(ytunnusStr,
                    kieli.value(),
                    asiakastunnus,
                    aikaleima,
                    tarkiste,
                    TIKETTI);

        } catch (SOAPFaultException exp) {
            LOG.error("Failed to invoke SOAP endpoint", exp);
            throw new YtjConnectionException(YtjExceptionType.SOAP, exp.getFault().getFaultString());
        } catch (Exception commonExp) {
            LOG.error("Failed to invoke SOAP endpoint", commonExp);
            throw new YtjConnectionException(YtjExceptionType.OTHER, commonExp.getMessage());
        }

        if (vastaus.getYritysTiedotV2DTO().get(0).getVirheTiedot() != null) {
            LOG.error("YTJ service returned null reply");
            throw new YtjConnectionException(YtjExceptionType.OTHER, DEFAULT_ERROR_MESSAGE);
        }

        return vastaus.getYritysTiedotV2DTO()
                .stream()
                .map(mapper::mapYritysTiedotV2DTOtoYTJDTO)
                .collect(Collectors.toList());
    }

    private YritysTiedotSoap getYritysTiedotSoap() {
        YritysTiedot yt = new YritysTiedot();
        YritysTiedotSoap ytj = yt.getYritysTiedotSoap12();
        BindingProvider bindingProvider = (BindingProvider) ytj;
        bindingProvider.getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                ytjUrl);
        return ytj;
    }

}
