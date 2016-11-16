package fi.vm.sade.organisaatio.config;

import fi.vm.sade.properties.OphProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author simok
 */
@Controller
public class ConfigController {

    @Autowired
    private OphProperties ophProperties;

    @Autowired
    UrlConfiguration urlConfiguration;

    @RequestMapping(value = "/configuration.js", method = RequestMethod.GET, produces = "text/javascript")
    @ResponseBody
    public String index() {
        StringBuilder b = new StringBuilder();
        append(b, "UI_URL_BASE", ophProperties.getProperty("organisaatio-ui.organisaatio-ui-url"));
        append(b, "SERVICE_URL_BASE", ophProperties.getProperty("organisaatio-service.url.rest"));
        append(b, "KOODISTO_URL_BASE", ophProperties.getProperty("koodisto-service.url.rest"));
        append(b, "LOKALISAATIO_URL_BASE", ophProperties.getProperty("lokalisointi.url.rest"));
        append(b, "V1_LOKALISAATIO_URL", ophProperties.getProperty("lokalisointi.url.lokalisointi.v1"));

        append(b, "AUTHENTICATION_URL_BASE", ophProperties.getProperty("authentication-service-url.rest"));
        append(b, "ROOT_ORGANISAATIO_OID", ophProperties.getProperty("root.organisaatio.oid"));

        append(b, "TEMPLATE_URL_BASE", "");

        append(b, "CAS_URL", ophProperties.getOrElse("organisaatio-ui.cas.url", "/cas/myroles"));
        append(b, "CAS_ME_URL", ophProperties.getOrElse("organisaatio-ui.cas-me.url", "/cas/me"));
        append(b, "SESSION_KEEPALIVE_INTERVAL_IN_SECONDS", ophProperties.getOrElse("organisaatio-ui.session-keepalive-interval.seconds", "30"));
        append(b, "MAX_SESSION_IDLE_TIME_IN_SECONDS", ophProperties.getOrElse("organisaatio-ui.max-session-idle-time.seconds", "1800"));

        append(b, "ORGANISAATIO_REST", ophProperties.getProperty("organisaatio-service.rest.organisaatio"));
        append(b, "ORGANISAATIO_REST_HAE", ophProperties.getProperty("organisaatio-service.rest.organisaatio.param", "hae"));
        append(b, "ORGANISAATIO_REST_BY_OID", ophProperties.getProperty("organisaatio-service.rest.organisaatio.param", ":oid"));

        append(b, "ORGANISAATIO_REST_ORGAISAATIO_MAXINACTIVEINTERVAL", ophProperties.getProperty("organisaatio-service.rest.organisaatio.session.maxinactiveinterval"));
        append(b, "ORGANISAATIO_REST_YTJ_LOKI", ophProperties.getProperty("organisaatio-service.rest.organisaatio.ytjloki"));


        append(b, "ORGANISAATIO_REST_V2_HAE", ophProperties.getProperty("organisaatio-service.rest.organisaatio.v2.param", "hae"));
        append(b, "ORGANISAATIO_REST_V2_RYHMAT", ophProperties.getProperty("organisaatio-service.rest.organisaatio.v2.param", "ryhmat"));
        append(b, "ORGANISAATIO_REST_V2_MUOKKAAMONTA", ophProperties.getProperty("organisaatio-service.rest.organisaatio.v2.param", "muokkaamonta"));


        append(b, "ORGANISAATIO_REST_V2_OID_HISTORIA", ophProperties.getProperty("organisaatio-service.rest.organisaatio.v2.params", ":oid", "historia"));
        append(b, "ORGANISAATIO_REST_V2_OID_ORGANISAATIOSUHDE", ophProperties.getProperty("organisaatio-service.rest.organisaatio.v2.params", ":oid", "organisaatiosuhde"));
        append(b, "ORGANISAATIO_REST_V2_HIERARKIA_HAE", ophProperties.getProperty("organisaatio-service.rest.organisaatio.v2.params", "hierarkia", "hae"));
        append(b, "ORGANISAATIO_REST_V2_PAIVITTAJA_HAE", ophProperties.getProperty("organisaatio-service.rest.organisaatio.v2.params", ":oid", "paivittaja"));
        append(b, "ORGANISAATIO_REST_V2_NIMIHISTORIA_HAE", ophProperties.getProperty("organisaatio-service.rest.organisaatio.v2.nimihistoria.haku"));

        append(b, "ORGANISAATIO_REST_AUTH", ophProperties.getProperty("organisaatio-service.rest.organisaatio.param", "auth"));
        append(b, "ORGANISAATIO_REST_YTJ_YTUNNUS", ophProperties.getProperty("organisaatio-service.rest.organisaatio.ytj", ":ytunnus"));
//        append(b, "ORGANISAATIO_REST_YTJ_HAE", ophProperties.getProperty("organisaatio-service.rest.organisaatio.ytj", "hae"));
        append(b, "ORGANISAATIO_REST_YTJ_HAE", ophProperties.getProperty("organisaatio-service.rest.organisaatio.ytjHae"));

//        append(b, "ORGANISAATIO_REST_YHTEYSTIETOJENTYYPPI", ophProperties.getProperty("organisaatio-service.rest.organisaatio.yhteystietojentyyppi"));
        append(b, "ORGANISAATIO_REST_YHTEYSTIETOJENTYYPPI", ophProperties.getProperty("organisaatio-service.rest.organisaatio.yhteystietojentyyppi"));

        append(b, "ORGANISAATIO_REST_YHTEYSTIETOJENTYYPPI_BY_OID", ophProperties.getProperty("organisaatio-service.rest.organisaatio.yhteystietojentyyppiByOid", ":oid"));
        append(b, "ORGANISAATIO_REST_YTJ_LOKI", ophProperties.getProperty("organisaatio-service.rest.organisaatio.ytjloki"));

        append(b, "AUTHENTICATION_REST_HENKILO", ophProperties.getProperty("authentication-service.henkilo"));
        append(b, "AUTHENTICATION_REST_HENKILO_BY_OID", ophProperties.getProperty("authentication-service.henkilo.byOid"));
        append(b, "AUTHENTICATION_REST_RYHMA_BY_HENKILO_OID", ophProperties.getProperty("authentication-service.henkilo.ryhma"));


        append(b, "KOODISTO_ORGANISAATIOTYYPPI_KOODI", ophProperties.getProperty("koodisto-service.url.rest.json", "organisaatiotyyppi", "koodi"));
        append(b, "KOODISTO_OPPILAITOSTYYPPI_KOODI", ophProperties.getProperty("koodisto-service.url.rest.json", "oppilaitostyyppi", "koodi"));
        append(b, "KOODISTO_MAAT_JA_VALTIOT", ophProperties.getProperty("koodisto-service.url.rest.json", "maatjavaltiot1", "koodi"));
        append(b, "KOODISTO_KIELI_KOODI", ophProperties.getProperty("koodisto-service.url.rest.json", "kieli", "koodi"));
        append(b, "KOODISTO_VUOSILUOKAT", ophProperties.getProperty("koodisto-service.url.rest.json", "vuosiluokat", "koodi"));
        append(b, "KOODISTO_URI_KOODI", ophProperties.getProperty("koodisto-service.url.rest.json", ":uri", "koodi"));
        append(b, "KOODISTO_POSTI_KOODI", ophProperties.getProperty("koodisto-service.url.rest.json", "posti", "koodi"));
        append(b, "KOODISTO_KUNTA_KOODI", ophProperties.getProperty("koodisto-service.url.rest.kunta.koodi"));
        append(b, "KOODISTO_KOODI_HAE", ophProperties.getProperty("koodisto-service.url.rest.search.koodis"));
        append(b, "KOODISTO_POSTI", ophProperties.getProperty("koodisto-service.url.rest.posti"));
        append(b, "KOODISTO_OPPILAITOKSENOPETUSKIELI", ophProperties.getProperty("koodisto-service.url.rest.oppilaitoksenopetuskieli.koodi"));


        if (ophProperties.getProperty("auth.mode") != null && !ophProperties.getProperty("auth.mode").isEmpty()) {
            append(b, "AUTH_MODE", ophProperties.getProperty("auth.mode"));
        }

        return b.toString();
    }

    private void append(StringBuilder b, String key, String value) {
        b.append(key);
        b.append(" = \"");
        b.append(value);
        b.append("\";\n");
    }

}
