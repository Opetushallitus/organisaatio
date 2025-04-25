package fi.vm.sade.organisaatio.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.Organisaatio;

import java.util.Map.Entry;

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
 * <li>Jos organisaatio on VARHAISKASVATUKSEN_JARJESTAJA ja sille on
 * määritelty yläorganisaatio, on yläorganisaation oltava OPH.
 * <li>Jos organisaatio on VARHAISKASVATUKSEN_TOIMIPAIKKA, sillä on oltava
 * yläorganisaatio joka on tyypiltään VARHAISKASVATUKSEN_JARJESTAJA tai VARHAISKASVATUKSEN_TOIMIPAIKKA.
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
            return parentChild.getValue().getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS.koodiValue())
                    && parentChild.getKey() != null
                    && parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.KOULUTUSTOIMIJA.koodiValue());
        }
    };

    Predicate<Entry<Organisaatio, Organisaatio>> muuOrgRule = new Predicate<Entry<Organisaatio, Organisaatio>>() {
        @Override
        public boolean apply(Entry<Organisaatio, Organisaatio> parentChild) {
            return parentChild.getValue().getTyypit().contains(OrganisaatioTyyppi.MUU_ORGANISAATIO.koodiValue())
                    && (parentChild.getKey() == null
                    || ophOid.equals(parentChild.getKey().getOid())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.MUU_ORGANISAATIO.koodiValue()));
        }
    };

    Predicate<Entry<Organisaatio, Organisaatio>> varhaiskasvatuksenJarjestajaRule = new Predicate<Entry<Organisaatio, Organisaatio>>() {
        @Override
        public boolean apply(Entry<Organisaatio, Organisaatio> parentChild) {
            return parentChild.getValue().getTyypit().contains(OrganisaatioTyyppi.VARHAISKASVATUKSEN_JARJESTAJA.koodiValue())
                    && (parentChild.getKey() == null
                    || ophOid.equals(parentChild.getKey().getOid()));
        }
    };

    Predicate<Entry<Organisaatio, Organisaatio>> varhaiskasvatuksenToimipaikkaRule = new Predicate<Entry<Organisaatio, Organisaatio>>() {
        @Override
        public boolean apply(Entry<Organisaatio, Organisaatio> parentChild) {
            return parentChild.getValue().getTyypit().contains(OrganisaatioTyyppi.VARHAISKASVATUKSEN_TOIMIPAIKKA.koodiValue())
                    && parentChild.getKey() != null
                    && (parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.VARHAISKASVATUKSEN_JARJESTAJA.koodiValue())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.VARHAISKASVATUKSEN_TOIMIPAIKKA.koodiValue()));
        }
    };

    Predicate<Entry<Organisaatio, Organisaatio>> tyoelamajarjestoRule = new Predicate<Entry<Organisaatio, Organisaatio>>() {
        @Override
        public boolean apply(Entry<Organisaatio, Organisaatio> parentChild) {
            return parentChild.getValue().getTyypit().contains(OrganisaatioTyyppi.TYOELAMAJARJESTO.koodiValue())
                    && (parentChild.getKey() == null
                    || ophOid.equals(parentChild.getKey().getOid())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.TYOELAMAJARJESTO.koodiValue()));
        }
    };

    Predicate<Entry<Organisaatio, Organisaatio>> koulutustoimijaRule = new Predicate<Entry<Organisaatio, Organisaatio>>() {
        @Override
        public boolean apply(Entry<Organisaatio, Organisaatio> parentChild) {
            return parentChild.getValue().getTyypit().contains(OrganisaatioTyyppi.KOULUTUSTOIMIJA.koodiValue())
                    && (parentChild.getKey() == null
                    || ophOid.equals(parentChild.getKey().getOid()));
        }
    };

    Predicate<Entry<Organisaatio, Organisaatio>> toimipisteRule = new Predicate<Entry<Organisaatio, Organisaatio>>() {
        @Override
        public boolean apply(Entry<Organisaatio, Organisaatio> parentChild) {
            return parentChild.getValue().getTyypit().contains(OrganisaatioTyyppi.TOIMIPISTE.koodiValue())
                    && parentChild.getKey() != null
                    && (parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS.koodiValue())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.TOIMIPISTE.koodiValue())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.MUU_ORGANISAATIO.koodiValue())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.TYOELAMAJARJESTO.koodiValue()));
        }
    };

    Predicate<Entry<Organisaatio, Organisaatio>> oppisopimustoimipisteRule = new Predicate<Entry<Organisaatio, Organisaatio>>() {
        @Override
        public boolean apply(Entry<Organisaatio, Organisaatio> parentChild) {
            return parentChild.getValue().getTyypit().contains(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.koodiValue())
                    && parentChild.getKey() != null
                    && parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.KOULUTUSTOIMIJA.koodiValue());
        }
    };

    Predicate<Entry<Organisaatio, Organisaatio>> ryhmaRule = new Predicate<Entry<Organisaatio, Organisaatio>>() {
        @Override
        public boolean apply(Entry<Organisaatio, Organisaatio> parentChild) {
            return parentChild.getValue().getTyypit().contains(OrganisaatioTyyppi.RYHMA.koodiValue())
                    && parentChild.getKey() != null
                    && (parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.KOULUTUSTOIMIJA.koodiValue())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS.koodiValue())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.TOIMIPISTE.koodiValue())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE.koodiValue())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.MUU_ORGANISAATIO.koodiValue())
                    || parentChild.getKey().getTyypit().contains(OrganisaatioTyyppi.TYOELAMAJARJESTO.koodiValue()));
        }
    };

    Predicate<Entry<Organisaatio, Organisaatio>> kuntaRule = new Predicate<Entry<Organisaatio, Organisaatio>>() {
        @Override
        public boolean apply(Entry<Organisaatio, Organisaatio> parentChild) {
            return parentChild.getValue().getTyypit().contains(OrganisaatioTyyppi.KUNTA.koodiValue())
                    && (parentChild.getKey() == null
                    || ophOid.equals(parentChild.getKey().getOid()));
        }
    };

    public OrganisationHierarchyValidator(final String ophOid) {
        Preconditions.checkNotNull(ophOid);
        this.ophOid = ophOid;

    }

    @Override
    public boolean apply(Entry<Organisaatio, Organisaatio> parentChild) {
        Preconditions.checkNotNull(parentChild);
        return Predicates.or(oppilaitosRule, muuOrgRule, varhaiskasvatuksenJarjestajaRule,
                varhaiskasvatuksenToimipaikkaRule, tyoelamajarjestoRule, toimipisteRule, koulutustoimijaRule,
                oppisopimustoimipisteRule, ryhmaRule, kuntaRule).apply(parentChild);
    }

}
