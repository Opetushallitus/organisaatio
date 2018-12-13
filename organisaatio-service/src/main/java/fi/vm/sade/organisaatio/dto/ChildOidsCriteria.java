package fi.vm.sade.organisaatio.dto;

import java.time.LocalDate;

import static java.util.Objects.requireNonNull;

public class ChildOidsCriteria {

    private final String oid;
    private final boolean aktiiviset;
    private final boolean suunnitellut;
    private final boolean lakkautetut;
    private final LocalDate paivamaara;

    public ChildOidsCriteria(String oid, boolean aktiiviset, boolean suunnitellut, boolean lakkautetut, LocalDate paivamaara) {
        this.oid = requireNonNull(oid);
        this.aktiiviset = aktiiviset;
        this.suunnitellut = suunnitellut;
        this.lakkautetut = lakkautetut;
        this.paivamaara = requireNonNull(paivamaara);
    }

    public String getOid() {
        return oid;
    }

    public boolean isAktiiviset() {
        return aktiiviset;
    }

    public boolean isSuunnitellut() {
        return suunnitellut;
    }

    public boolean isLakkautetut() {
        return lakkautetut;
    }

    public LocalDate getPaivamaara() {
        return paivamaara;
    }

}
