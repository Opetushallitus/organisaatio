package fi.vm.sade.organisaatio.business;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.*;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.resource.dto.HakutoimistoDTO;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Predicates.and;
import static fi.vm.sade.organisaatio.model.Osoite.*;

public class Hakutoimisto {
    private static final Predicate<Yhteystieto> anyKayntiosoite = Predicates.or(osoitetyyppiPredicate(TYYPPI_KAYNTIOSOITE), osoitetyyppiPredicate(TYYPPI_ULKOMAINEN_KAYNTIOSOITE));
    private static final Predicate<Yhteystieto> anyPostiosoite = Predicates.or(osoitetyyppiPredicate(TYYPPI_POSTIOSOITE), osoitetyyppiPredicate(TYYPPI_ULKOMAINEN_POSTIOSOITE));

    public static Map<String, String> hakutoimistonNimet(Organisaatio organisaatio) {
        MonikielinenTeksti hakutoimistoNimi = organisaatio.getMetadata().getHakutoimistoNimi();
        return hakutoimistoNimi != null ? hakutoimistoNimi.getValues() : ImmutableMap.<String, String>of();
    }

    public static Map<String, HakutoimistoDTO.HakutoimistonYhteystiedotDTO> hakutoimistonOsoitteet(Organisaatio organisaatio) {
        ImmutableMap<String, Collection<Yhteystieto>> grouped = groupYhteystiedot(organisaatio).asMap();

        return Maps.transformEntries(grouped, new Maps.EntryTransformer<String, Collection<Yhteystieto>, HakutoimistoDTO.HakutoimistonYhteystiedotDTO>() {
            @Override
            public HakutoimistoDTO.HakutoimistonYhteystiedotDTO transformEntry(@Nullable String key, @Nullable Collection<Yhteystieto> value) {
                return hakutoimistonYhteystiedot(value, key);
            }
        });
    }

    public static boolean hasKayntiosoite(Organisaatio organisaatio) {
        return organisaatio.getMetadata() != null && findYhteystieto(organisaatio.getMetadata().getYhteystiedot(), anyKayntiosoite).isPresent();
    }

    private static ImmutableListMultimap<String, Yhteystieto> groupYhteystiedot(Organisaatio organisaatio) {
        return Multimaps.index(organisaatio.getMetadata().getYhteystiedot(), new Function<Yhteystieto, String>() {
            @Nullable
            @Override
            public String apply(@Nullable Yhteystieto input) {
                return input.getKieli();
            }
        });
    }

    private static HakutoimistoDTO.HakutoimistonYhteystiedotDTO hakutoimistonYhteystiedot(Iterable<Yhteystieto> yhteystiedot, String lang) {
        Optional<HakutoimistoDTO.OsoiteDTO> kayntiosoite = yhteystietoToOsoiteDTO(findYhteystieto(yhteystiedot, and(osoitekieliPredicate(lang), anyKayntiosoite)));
        Optional<HakutoimistoDTO.OsoiteDTO> postiosoite = yhteystietoToOsoiteDTO(findYhteystieto(yhteystiedot, and(osoitekieliPredicate(lang), anyPostiosoite)));

        return new HakutoimistoDTO.HakutoimistonYhteystiedotDTO(
                kayntiosoite.orNull(),
                postiosoite.orNull(),
                www(yhteystiedot).orNull(),
                email(yhteystiedot).orNull(),
                puhelin(yhteystiedot).orNull());
    }

    private static Optional<String> email(Iterable<Yhteystieto> yhteystiedot) {
        return findYhteystieto(yhteystiedot, new Predicate<Yhteystieto>() {
            @Override
            public boolean apply(Yhteystieto input) {
                return input instanceof Email;
            }
        }).transform(new Function<Yhteystieto, String>() {
            @Nullable
            @Override
            public String apply(@Nullable Yhteystieto input) {
                return ((Email) input).getEmail();
            }
        });
    }

    private static Optional<String> puhelin(Iterable<Yhteystieto> yhteystiedot) {
        return findYhteystieto(yhteystiedot, new Predicate<Yhteystieto>() {
            @Override
            public boolean apply(Yhteystieto input) {
                return input instanceof Puhelinnumero;
            }
        }).transform(new Function<Yhteystieto, String>() {
            @Nullable
            @Override
            public String apply(@Nullable Yhteystieto input) {
                return ((Puhelinnumero) input).getPuhelinnumero();
            }
        });
    }

    private static Optional<String> www(Iterable<Yhteystieto> yhteystiedot) {
        return findYhteystieto(yhteystiedot, new Predicate<Yhteystieto>() {
            @Override
            public boolean apply(@Nullable Yhteystieto input) {
                return input instanceof Www;
            }
        }).transform(new Function<Yhteystieto, String>() {
            @Nullable
            @Override
            public String apply(@Nullable Yhteystieto input) {
                return ((Www) input).getWwwOsoite();
            }
        });
    }

    private static Optional<HakutoimistoDTO.OsoiteDTO> yhteystietoToOsoiteDTO(Optional<Yhteystieto> yhteystieto) {
        return yhteystieto.transform(new Function<Yhteystieto, HakutoimistoDTO.OsoiteDTO>() {
            @Nullable
            @Override
            public HakutoimistoDTO.OsoiteDTO apply(Yhteystieto input) {
                Osoite osoite = (Osoite) input;
                return new HakutoimistoDTO.OsoiteDTO(osoite.getYhteystietoOid(), osoite.getOsoite(), osoite.getPostinumero(), osoite.getPostitoimipaikka());
            }
        });
    }

    private static Predicate<Yhteystieto> osoitekieliPredicate(final String kieli) {
        return new Predicate<Yhteystieto>() {
            @Override
            public boolean apply(Yhteystieto input) {
                if (input instanceof Osoite) {
                    final Osoite osoite = (Osoite) input;
                    return kieli.equals(osoite.getKieli());
                }
                return false;
            }
        };
    }

    private static Predicate<Yhteystieto> osoitetyyppiPredicate(final String osoitetyyppi) {
        return new Predicate<Yhteystieto>() {
            @Override
            public boolean apply(Yhteystieto input) {
                if (input instanceof Osoite) {
                    final Osoite osoite = (Osoite) input;
                    return osoitetyyppi.equals(osoite.getOsoiteTyyppi());
                }
                return false;
            }
        };
    }

    private static Optional<Yhteystieto> findYhteystieto(Iterable<Yhteystieto> yhteystiedot, Predicate<Yhteystieto> predicate) {
        return Iterables.tryFind(yhteystiedot, predicate);
    }

}
