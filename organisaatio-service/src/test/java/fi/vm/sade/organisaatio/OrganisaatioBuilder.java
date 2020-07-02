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
import static java.util.stream.Collectors.toSet;

public class OrganisaatioBuilder<T extends OrganisaatioBuilder<T>> {

    private final String oid;
    private final List<OrganisaatioTyyppi> tyypit = new ArrayList<>();
    private final MonikielinenTeksti nimi = new MonikielinenTeksti();
    private final List<String> opetuskielet = new ArrayList<>();
    private java.sql.Date alkuPvm;
    private java.sql.Date lakkautusPvm;
    private String kotipaikka;
    private String maa;
    private Organisaatio parent;

    public OrganisaatioBuilder(String oid) {
        this.oid = requireNonNull(oid);
    }

    @SuppressWarnings("unchecked")
    private T builder() {
        return (T) this;
    }

    public T tyyppi(OrganisaatioTyyppi... tyyppi) {
        tyypit.addAll(Arrays.asList(tyyppi));
        return builder();
    }

    public T nimi(String kieli, String arvo) {
        this.nimi.addString(kieli, arvo);
        return builder();
    }

    public T opetuskieli(String opetuskieli) {
        this.opetuskielet.add(opetuskieli);
        return builder();
    }

    public T alkuPvm(LocalDate alkuPvm) {
        this.alkuPvm = java.sql.Date.valueOf(alkuPvm);
        return builder();
    }

    public T lakkautusPvm(LocalDate lakkautusPvm) {
        this.lakkautusPvm = java.sql.Date.valueOf(lakkautusPvm);
        return builder();
    }

    public T kotipaikka(String kotipaikka) {
        this.kotipaikka = kotipaikka;
        return builder();
    }

    public T maa(String maa) {
        this.maa = maa;
        return builder();
    }

    public T parent(Organisaatio parent) {
        this.parent = parent;
        return builder();
    }

    public Organisaatio build() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setOid(oid);
        organisaatio.setTyypit(tyypit.stream().map(OrganisaatioTyyppi::value).collect(toSet()));
        organisaatio.setNimi(nimi);
        organisaatio.setNimihaku(String.join(",", nimi.getValues().values()));
        organisaatio.setKielet(opetuskielet);
        organisaatio.setAlkuPvm(alkuPvm);
        organisaatio.setLakkautusPvm(lakkautusPvm);
        organisaatio.setKotipaikka(kotipaikka);
        organisaatio.setMaa(maa);
        if (parent != null) {
            List<OrganisaatioSuhde> parentSuhteet = new ArrayList<>();
            OrganisaatioSuhde parentSuhde = new OrganisaatioSuhde();
            parentSuhde.setChild(organisaatio);
            parentSuhde.setParent(parent);
            parentSuhteet.add(parentSuhde);
            organisaatio.setParentSuhteet(parentSuhteet);
            organisaatio.setParentOids(generateParentOids(parent));
            organisaatio.setParentIdPath(generateParentPath(parent.getParentIdPath(), parent.getId()));
            organisaatio.setParentOids(generateParentOids(parent));
        }
        return organisaatio;
    }

    private static String generateParentPath(String parentParentPath, Serializable parentId) {
        return Optional.ofNullable(parentParentPath).map(path -> path + parentId + "|").orElse("|" + parentId + "|");
    }

    private static List<String> generateParentOids(Organisaatio parent) {
        List<String> parentOids = new ArrayList<>();
        parentOids.add(parent.getOid());
        parentOids.addAll(parent.getParentOids());
        return parentOids;
    }

}
