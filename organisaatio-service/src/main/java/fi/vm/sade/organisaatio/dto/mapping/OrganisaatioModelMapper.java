package fi.vm.sade.organisaatio.dto.mapping;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioYhteystiedotDTOV2;
import fi.vm.sade.organisaatio.dto.v2.OsoiteDTOV2;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.service.util.YhteystietoUtil;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class OrganisaatioModelMapper extends ModelMapper {
    private final OsoiteModelMapper modelMapper;

    public OrganisaatioModelMapper(OsoiteModelMapper modelMapper) {
        super();
        this.modelMapper = modelMapper;

        // PostiOsoiteConverter
        final Converter<Set<Yhteystieto>, Set<OsoiteDTOV2>> postiOsoiteConverter = mc -> {
            // Define the target type for mapping
            Type osoiteDTOV2Type = new TypeToken<Set<OsoiteDTOV2>>() {}.getType();

            Set<Osoite> postiOsoitteet = YhteystietoUtil.getPostiOsoitteet(mc.getSource());

            // Map domain type to DTO
            return modelMapper.map(postiOsoitteet, osoiteDTOV2Type);
        };

        // KayntiOsoiteConverter
        final Converter<Set<Yhteystieto>, Set<OsoiteDTOV2>> kayntiOsoiteConverter = mc -> {
            // Define the target type for mapping
            Type osoiteDTOV2Type = new TypeToken<Set<OsoiteDTOV2>>() {}.getType();

            Set<Osoite> postiOsoitteet = YhteystietoUtil.getKayntiOsoitteet(mc.getSource());

            // Map domain type to DTO
            return modelMapper.map(postiOsoitteet, osoiteDTOV2Type);
        };

        // wwwOsoiteConverter
        final Converter<Set<Yhteystieto>, Map<String, String>> wwwOsoiteConverter = mc -> {
            Set<Www> wwwOsoitteet = YhteystietoUtil.getWwwOsoitteet(mc.getSource());

            // Tehdään map, jossa avaimena kieli ja arvone www osoite
            Map<String, String> wwwOsoiteMap = new HashMap<>();

            for (Www www : wwwOsoitteet) {
                wwwOsoiteMap.put(www.getKieli(), www.getWwwOsoite());
            }

            return wwwOsoiteMap;
        };

        // wwwOsoiteConverter
        final Converter<Set<Yhteystieto>, Map<String, String>> emailOsoiteConverter = mc -> {
            Set<Email> emailOsoitteet = YhteystietoUtil.getEmailOsoitteet(mc.getSource());

            // Tehdään map, jossa avaimena kieli ja arvona email osoite
            Map<String, String> emailOsoiteMap = new HashMap<>();

            for (Email email : emailOsoitteet) {
                emailOsoiteMap.put(email.getKieli(), email.getEmail());
            }

            return emailOsoiteMap;
        };

        // puhelinnumeroConverter
        final Converter<Set<Yhteystieto>, Map<String, String>> puhelinnumeroConverter = mc -> {
            Set<Puhelinnumero> puhelinnumerot = YhteystietoUtil.getPuhelinnumerot(mc.getSource());

            // Tehdään map, jossa avaimena kieli ja arvone puhelinnumero
            Map<String, String> puhelinnumeroMap = new HashMap<>();

            for (Puhelinnumero numero : puhelinnumerot) {
                puhelinnumeroMap.put(numero.getKieli(), numero.getPuhelinnumero());
            }

            return puhelinnumeroMap;
        };

        final Converter<Organisaatio, Set<String>> tyypitConverter = mc -> OrganisaatioTyyppi.fromKoodiToValue(mc.getSource().getTyypit());

        this.addMappings(new PropertyMap<Organisaatio, OrganisaatioYhteystiedotDTOV2>() {
            @Override
            protected void configure() {
                // Monikielinen nimi
                map().setNimi(source.getNimi().getValues());

                // Postiosoite
                using(postiOsoiteConverter).map(source.getYhteystiedot()).setPostiosoite(null);

                // Käyntiosoite
                using(kayntiOsoiteConverter).map(source.getYhteystiedot()).setKayntiosoite(null);

                // Puhelinnumero
                using(puhelinnumeroConverter).map(source.getYhteystiedot()).setPuhelinnumero(null);

                // WWW-osoite
                using(wwwOsoiteConverter).map(source.getYhteystiedot()).setWwwOsoite(null);

                // Email-osoite
                using(emailOsoiteConverter).map(source.getYhteystiedot()).setEmailOsoite(null);

                // Tyypit koodiarvoista organisaatiopalvelun formaattiin
                using(tyypitConverter).map(source).setTyypit(new HashSet<>());
            }
        });
    }
}
