package fi.vm.sade.organisaatio;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;

import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public final class RyhmaBuilder {

    private final String oid;
    private final Set<String> ryhmatyypit = new LinkedHashSet<>();
    private final Set<String> kayttoryhmat = new LinkedHashSet<>();
    private final MonikielinenTeksti nimi = new MonikielinenTeksti();
    private final MonikielinenTeksti kuvaus2 = new MonikielinenTeksti();
    private LocalDate lakkautusPvm;
    private boolean poistettu;
    private Organisaatio parent;

    public RyhmaBuilder(String oid) {
        this.oid = requireNonNull(oid);
    }

    public RyhmaBuilder ryhmatyyppi(String... ryhmatyyppi) {
        Arrays.stream(ryhmatyyppi).forEach(this.ryhmatyypit::add);
        return this;
    }

    public RyhmaBuilder kayttoryhma(String... kayttoryhma) {
        Arrays.stream(kayttoryhma).forEach(this.kayttoryhmat::add);
        return this;
    }

    public RyhmaBuilder nimi(String kieli, String arvo) {
        this.nimi.addString(kieli, arvo);
        return this;
    }

    public RyhmaBuilder kuvaus2(String kieli, String arvo) {
        this.kuvaus2.addString(kieli, arvo);
        return this;
    }

    public RyhmaBuilder lakkautusPvm(LocalDate lakkautusPvm) {
        this.lakkautusPvm = lakkautusPvm;
        return this;
    }

    public RyhmaBuilder poistettu() {
        this.poistettu = true;
        return this;
    }

    public RyhmaBuilder parent(Organisaatio parent) {
        this.parent = parent;
        return this;
    }

    public Organisaatio build() {
        Organisaatio ryhma = new Organisaatio();
        ryhma.setOid(oid);
        ryhma.setTyypit(Stream.of(OrganisaatioTyyppi.RYHMA).map(OrganisaatioTyyppi::koodiValue).collect(Collectors.toSet()));
        ryhma.setOrganisaatiotyypitStr("Ryhma|");
        ryhma.setRyhmatyypit(ryhmatyypit);
        ryhma.setKayttoryhmat(kayttoryhmat);
        ryhma.setNimi(nimi);
        ryhma.setKuvaus2(kuvaus2);
        ryhma.setLakkautusPvm(Optional.ofNullable(lakkautusPvm).map(Date::valueOf).orElse(null));
        ryhma.setOrganisaatioPoistettu(poistettu);
        if (parent != null) {
            List<OrganisaatioSuhde> parentSuhteet = new ArrayList<>();
            OrganisaatioSuhde parentSuhde = new OrganisaatioSuhde();
            parentSuhde.setChild(ryhma);
            parentSuhde.setParent(parent);
            parentSuhteet.add(parentSuhde);
            ryhma.setParentSuhteet(parentSuhteet);
            ryhma.setParentOidPath(generateParentPath(parent.getParentOidPath(), parent.getOid()));
            ryhma.setParentIdPath(generateParentPath(parent.getParentIdPath(), parent.getId()));
        }
        return ryhma;
    }

    private static String generateParentPath(String parentParentPath, Serializable parentId) {
        return Optional.ofNullable(parentParentPath).map(path -> path + parentId + "|").orElse("|" + parentId + "|");
    }

}
