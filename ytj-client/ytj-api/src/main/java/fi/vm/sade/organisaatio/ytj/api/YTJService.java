package fi.vm.sade.organisaatio.ytj.api;


import fi.vm.sade.organisaatio.ytj.api.exception.YtjConnectionException;
import java.util.List;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

@WebService
public interface YTJService {

    @WebMethod
    YTJDTO findByYTunnus(@WebParam String ytunnus, YTJKieli kieli) throws YtjConnectionException;

    @WebMethod
    List<YTJDTO> findByYTunnusBatch(@WebParam List<String> ytunnuses, YTJKieli ytjKieli) throws YtjConnectionException;

    @WebMethod
    List<YTJDTO> findByYNimi(@WebParam String nimi, @WebParam boolean naytaPassiiviset, @WebParam YTJKieli kieli) throws YtjConnectionException;

}
