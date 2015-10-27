package fi.vm.sade.organisaatio.service;

import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.Organisaatio;

/**
 * Organisaatiohierarkiasäännöt:
 *
 * <li>Jos organisaatio on OPPILAITOS, sillä on oltava yläorganisaatio
 * tyypiltään KOULUTUSTOIMIJA.
 * <li>Jos organisaatio on MUU ORGANISAATIO ja sille on
 * määritelty yläorganisaatio, on yläorganisaation oltava joko OPH tai MUU
 * ORGANISAATIO.
 * <li>Jos organisaatio on KOULUTUSTOIMIJA ja sille on
 * määritelty yläorganisaatio, on yläorganisaation oltava joko OPH tai
 * KOULUTUSTOIMIJA
 * <li>Jos organisaatio on TYÖELÄMÄJÄRJESTÖ ja sille on
 * määritelty yläorganisaatio, on yläorganisaation oltava joko OPH tai
 * TYÖELÄMÄJÄRJESTÖ.
 * <li>Jos organisaatio on TOIMIPISTE, sillä on oltava
 * yläorganisaatio joka on tyypiltään joko TOIMIPISTE, OPPILAITOS,
 * MUU ORGANISAATIO tai TYÖELÄMÄJÄRJESTÖ.
 * <li>Jos organisaatio on OPPISOPIMUSTOIMIPISTE, sillä on oltava
 * yläorganisaatio joka on tyypiltään KOULUTUSTOIMIJA.
 *
 */
public class OrganisationHierarchyValidator implements Predicate<Entry<Organisaatio, Organisaatio>> {

    private final String ophOid;

    Predicate<Entry<Organisaatio, Organisaatio>> oppilaitosRule = new Predicate<Entry<Organisaatio, Organisaatio>>() {
        @Override
        public boolean apply(Entry<Organisaatio, Organisaatio> parentChild) {
            return parentChild.getValue().getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS.value())
                    && parentChild.getKey() != null
                    && parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.KOULUTUSTOIMIJA.value());
        }
    };

    Predicate<Entry<Organisaatio, Organisaatio>> muuOrgRule = new Predicate<Entry<Organisaatio, Organisaatio>>() {
        @Override
        public boolean apply(Entry<Organisaatio, Organisaatio> parentChild) {
            return parentChild.getValue().getTyypit().contains(OrganisaatioTyyppi.MUU_ORGANISAATIO.value())
                    && (parentChild.getKey() == null
                    || ophOid.equals(parentChild.getKey().getOid())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.MUU_ORGANISAATIO.value()));
        }
    };
    // XXX työelämäjärjestö
    Predicate<Entry<Organisaatio, Organisaatio>> tyoelamajarjestoRule = new Predicate<Entry<Organisaatio, Organisaatio>>() {
        @Override
        public boolean apply(Entry<Organisaatio, Organisaatio> parentChild) {
            return parentChild.getValue().getTyypit().contains(OrganisaatioTyyppi.TYOELAMAJARJESTO.value())
                    && (parentChild.getKey() == null
                    || ophOid.equals(parentChild.getKey().getOid())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.TYOELAMAJARJESTO.value()));
        }
    };

    Predicate<Entry<Organisaatio, Organisaatio>> koulutustoimijaRule = new Predicate<Entry<Organisaatio, Organisaatio>>() {
        @Override
        public boolean apply(Entry<Organisaatio, Organisaatio> parentChild) {
            return parentChild.getValue().getTyypit().contains(OrganisaatioTyyppi.KOULUTUSTOIMIJA.value())
                    && (parentChild.getKey() == null
                    || ophOid.equals(parentChild.getKey().getOid()));
        }
    };

    Predicate<Entry<Organisaatio, Organisaatio>> toimipisteRule = new Predicate<Entry<Organisaatio, Organisaatio>>() {
        @Override
        public boolean apply(Entry<Organisaatio, Organisaatio> parentChild) {
            return parentChild.getValue().getTyypit().contains(OrganisaatioTyyppi.TOIMIPISTE.value())
                    && parentChild.getKey() != null
                    && (parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS.value())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.TOIMIPISTE.value())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.MUU_ORGANISAATIO.value())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.TYOELAMAJARJESTO.value()));
        }
    };

    Predicate<Entry<Organisaatio, Organisaatio>> oppisopimustoimipisteRule = new Predicate<Entry<Organisaatio, Organisaatio>>() {
        @Override
        public boolean apply(Entry<Organisaatio, Organisaatio> parentChild) {
            return parentChild.getValue().getTyypit().contains(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.value())
                    && parentChild.getKey() != null
                    && parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.KOULUTUSTOIMIJA.value());
        }
    };

    Predicate<Entry<Organisaatio, Organisaatio>> ryhmaRule = new Predicate<Entry<Organisaatio, Organisaatio>>() {
        @Override
        public boolean apply(Entry<Organisaatio, Organisaatio> parentChild) {
            return parentChild.getValue().getTyypit().contains(OrganisaatioTyyppi.RYHMA.value())
                    && parentChild.getKey() != null
                    && (parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.KOULUTUSTOIMIJA.value())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS.value())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.TOIMIPISTE.value())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.value())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.MUU_ORGANISAATIO.value())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.TYOELAMAJARJESTO.value()));
        }
    };

    public OrganisationHierarchyValidator(final String ophOid) {
        Preconditions.checkNotNull(ophOid);
        this.ophOid = ophOid;

    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean apply(Entry<Organisaatio, Organisaatio> parentChild) {
        Preconditions.checkNotNull(parentChild);
        return Predicates.or(oppilaitosRule, muuOrgRule, tyoelamajarjestoRule, toimipisteRule, koulutustoimijaRule, oppisopimustoimipisteRule, ryhmaRule).apply(parentChild);
    }

}
