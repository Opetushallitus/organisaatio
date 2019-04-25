package fi.vm.sade.organisaatio;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

public final class OrganisaatioBuilder {

    private final String oid;
    private final List<OrganisaatioTyyppi> tyypit = new ArrayList<>();
    private final MonikielinenTeksti nimi = new MonikielinenTeksti();
    private java.sql.Date alkuPvm;
    private java.sql.Date lakkautusPvm;
    private Organisaatio parent;

    public OrganisaatioBuilder(String oid) {
        this.oid = requireNonNull(oid);
    }

    public OrganisaatioBuilder tyyppi(OrganisaatioTyyppi... tyyppi) {
        Arrays.stream(tyyppi).forEach(this.tyypit::add);
        return this;
    }

    public OrganisaatioBuilder alkuPvm(LocalDate alkuPvm) {
        this.alkuPvm = java.sql.Date.valueOf(alkuPvm);
        return this;
    }

    public OrganisaatioBuilder lakkautusPvm(LocalDate lakkautusPvm) {
        this.lakkautusPvm = java.sql.Date.valueOf(lakkautusPvm);
        return this;
    }

    public OrganisaatioBuilder parent(Organisaatio parent) {
        this.parent = parent;
        return this;
    }

    public Organisaatio build() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setOid(oid);
        organisaatio.setTyypit(tyypit.stream().map(OrganisaatioTyyppi::value).collect(toSet()));
        organisaatio.setNimi(nimi);
        organisaatio.setNimihaku(nimi.getValues().values().stream().collect(joining(",")));
        organisaatio.setAlkuPvm(alkuPvm);
        organisaatio.setLakkautusPvm(lakkautusPvm);
        if (parent != null) {
            List<OrganisaatioSuhde> parentSuhteet = new ArrayList<>();
            OrganisaatioSuhde parentSuhde = new OrganisaatioSuhde();
            parentSuhde.setChild(organisaatio);
            parentSuhde.setParent(parent);
            parentSuhteet.add(parentSuhde);
            organisaatio.setParentSuhteet(parentSuhteet);
            organisaatio.setParentOidPath(generateParentPath(parent.getParentOidPath(), parent.getOid()));
            organisaatio.setParentIdPath(generateParentPath(parent.getParentIdPath(), parent.getId()));
        }
        return organisaatio;
    }

    private static String generateParentPath(String parentParentPath, Serializable parentId) {
        return Optional.ofNullable(parentParentPath).map(path -> path + parentId + "|").orElse("|" + parentId + "|");
    }

}
