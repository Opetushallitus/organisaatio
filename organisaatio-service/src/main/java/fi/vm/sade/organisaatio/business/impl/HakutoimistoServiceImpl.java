package fi.vm.sade.organisaatio.business.impl;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import fi.vm.sade.organisaatio.business.HakutoimistoService;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.business.exception.HakutoimistoNotFoundException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioNotFoundException;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.resource.dto.HakutoimistoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static fi.vm.sade.organisaatio.model.Osoite.*;

@Service
@RequiredArgsConstructor
public class HakutoimistoServiceImpl implements HakutoimistoService {
    private static final Predicate<Yhteystieto> anyKayntiosoite = osoitetyyppiPredicate(TYYPPI_KAYNTIOSOITE).or(osoitetyyppiPredicate(TYYPPI_ULKOMAINEN_KAYNTIOSOITE));
    private static final Predicate<Yhteystieto> anyPostiosoite = osoitetyyppiPredicate(TYYPPI_POSTIOSOITE).or(osoitetyyppiPredicate(TYYPPI_ULKOMAINEN_POSTIOSOITE));
    private final OrganisaatioFindBusinessService organisaatioFindBusinessService;

    @Override
    public HakutoimistoDTO hakutoimisto(String organisaatioOId) {
        return hakutoimistoRec(organisaatioOId);
    }
    private static Map<String, String> hakutoimistonNimet(Organisaatio organisaatio) {
        MonikielinenTeksti hakutoimistoNimi = organisaatio.getMetadata().getHakutoimistoNimi();
        return hakutoimistoNimi != null ? hakutoimistoNimi.getValues() : Map.of();
    }

    private static Map<String, HakutoimistoDTO.HakutoimistonYhteystiedotDTO> hakutoimistonOsoitteet(Organisaatio organisaatio) {
        ImmutableMap<String, Collection<Yhteystieto>> grouped = groupYhteystiedot(organisaatio).asMap();

        return Maps.transformEntries(grouped, (key, value) -> hakutoimistonYhteystiedot(value, key));
    }

    private static boolean hasOsoite(Organisaatio organisaatio) {
        return organisaatio.getMetadata() != null && findYhteystieto(organisaatio.getMetadata().getYhteystiedot(), anyPostiosoite.or(anyKayntiosoite)).isPresent();
    }

    private static ImmutableListMultimap<String, Yhteystieto> groupYhteystiedot(Organisaatio organisaatio) {
        return Multimaps.index(organisaatio.getMetadata().getYhteystiedot(), new Function<>() {
            @Nullable
            @Override
            public String apply(@Nullable Yhteystieto input) {
                return input != null ? input.getKieli() : null;
            }
        });
    }

    private static HakutoimistoDTO.HakutoimistonYhteystiedotDTO hakutoimistonYhteystiedot(Iterable<Yhteystieto> yhteystiedot, String lang) {
        Optional<HakutoimistoDTO.OsoiteDTO> kayntiosoite = yhteystietoToOsoiteDTO(findYhteystieto(yhteystiedot, osoitekieliPredicate(lang).and(anyKayntiosoite)));
        Optional<HakutoimistoDTO.OsoiteDTO> postiosoite = yhteystietoToOsoiteDTO(findYhteystieto(yhteystiedot, osoitekieliPredicate(lang).and(anyPostiosoite)));

        return new HakutoimistoDTO.HakutoimistonYhteystiedotDTO(
                kayntiosoite.orElse(null),
                postiosoite.orElse(null),
                www(yhteystiedot).orElse(null),
                email(yhteystiedot).orElse(null),
                puhelin(yhteystiedot).orElse(null));
    }

    private static Optional<String> email(Iterable<Yhteystieto> yhteystiedot) {
        return findYhteystieto(yhteystiedot, Email.class::isInstance).map(new Function<>() {
            @Nullable
            @Override
            public String apply(@Nullable Yhteystieto input) {
                return input != null ? ((Email) input).getEmail() : null;
            }
        });
    }

    private static Optional<String> puhelin(Iterable<Yhteystieto> yhteystiedot) {
        return findYhteystieto(yhteystiedot, Puhelinnumero.class::isInstance).map(new Function<>() {
            @Nullable
            @Override
            public String apply(@Nullable Yhteystieto input) {
                return input != null ? ((Puhelinnumero) input).getPuhelinnumero() : null;
            }
        });
    }

    private static Optional<String> www(Iterable<Yhteystieto> yhteystiedot) {
        return findYhteystieto(yhteystiedot, Www.class::isInstance).map(new Function<>() {
            @Nullable
            @Override
            public String apply(@Nullable Yhteystieto input) {
                return input != null ? ((Www) input).getWwwOsoite() : null;
            }
        });
    }

    private static Optional<HakutoimistoDTO.OsoiteDTO> yhteystietoToOsoiteDTO(Optional<Yhteystieto> yhteystieto) {
        return yhteystieto.map((Function<Yhteystieto, HakutoimistoDTO.OsoiteDTO>) input -> {
            Osoite osoite = (Osoite) input;
            return new HakutoimistoDTO.OsoiteDTO(osoite.getYhteystietoOid(), osoite.getOsoite(), osoite.getPostinumero(), osoite.getPostitoimipaikka());
        });
    }

    private static Predicate<Yhteystieto> osoitekieliPredicate(final String kieli) {
        return input -> {
            if (input instanceof Osoite) {
                final Osoite osoite = (Osoite) input;
                return kieli.equals(osoite.getKieli());
            }
            return false;
        };
    }

    private static Predicate<Yhteystieto> osoitetyyppiPredicate(final String osoitetyyppi) {
        return input -> {
            if (input instanceof Osoite) {
                final Osoite osoite = (Osoite) input;
                return osoitetyyppi.equals(osoite.getOsoiteTyyppi());
            }
            return false;
        };
    }

    private static Optional<Yhteystieto> findYhteystieto(Iterable<Yhteystieto> yhteystiedot, Predicate<Yhteystieto> predicate) {
        return StreamSupport.stream(yhteystiedot.spliterator(), false).filter(predicate).findFirst();
    }


    private HakutoimistoDTO hakutoimistoRec(String organisaatioOId) {

        Organisaatio organisaatio = organisaatioFindBusinessService.findById(organisaatioOId);
        if (organisaatio == null) {
            throw new OrganisaatioNotFoundException( organisaatioOId);
        }
        OrganisaatioMetaData metadata = organisaatio.getMetadata();
        return metadata == null ? hakutoimistoFromParent(organisaatio) : hakutoimistoFromOrganisaatio(organisaatio);
    }

    private HakutoimistoDTO hakutoimistoFromParent(Organisaatio organisaatio) {
        if (organisaatio.getParent() != null) {
            return hakutoimistoRec(organisaatio.getParent().getOid());
        }
        throw new HakutoimistoNotFoundException(organisaatio.getOid());
    }

    private HakutoimistoDTO hakutoimistoFromOrganisaatio(Organisaatio organisaatio) {
        if (hasOsoite(organisaatio)) {
            return new HakutoimistoDTO(hakutoimistonNimet(organisaatio), hakutoimistonOsoitteet(organisaatio));
        } else {
            return hakutoimistoFromParent(organisaatio);
        }
    }
}
