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
 * <li>Jos organisaatio on MUU ORGANISAATIO tai KOULUTUSTOMIJA ja sille on
 * määritelty yläorganisaatio, on yläorganisaation oltava joko OPH tai MUU
 * ORGANISAATIO.
 * <li>Jos organisaatio on OPETUSPISTE (eli toimipiste), sillä on oltava
 * yläorganisaatio joka on tyypiltään joko OPETUSPISTE, OPPILAITOS tai
 * KOULUTUSTOIMIJA.
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
            return parentChild.getValue().getTyypit().contains(OrganisaatioTyyppi.OPETUSPISTE.value())
                    && parentChild.getKey() != null
                    && (parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS.value())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.OPETUSPISTE.value())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.MUU_ORGANISAATIO.value()));
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

    public OrganisationHierarchyValidator(final String ophOid) {
        Preconditions.checkNotNull(ophOid);
        this.ophOid = ophOid;

    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean apply(Entry<Organisaatio, Organisaatio> parentChild) {
        Preconditions.checkNotNull(parentChild);
        return Predicates.or(oppilaitosRule, muuOrgRule, toimipisteRule, koulutustoimijaRule, oppisopimustoimipisteRule).apply(parentChild);
    }

}
