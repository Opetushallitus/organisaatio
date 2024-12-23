package fi.vm.sade.varda.rekisterointi.model;

import java.util.Objects;

public class VirkailijaDto {

    public String oid;
    public String etunimet;
    public String kutsumanimi;
    public String sukunimi;
    public String asiointikieli;
    public String sahkoposti;

    public VirkailijaDto() {
    }

    public VirkailijaDto(String oid, String asiointikieli, String sahkoposti) {
        this.oid = oid;
        this.asiointikieli = asiointikieli;
        this.sahkoposti = sahkoposti;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirkailijaDto that = (VirkailijaDto) o;
        return oid.equals(that.oid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oid);
    }
}
