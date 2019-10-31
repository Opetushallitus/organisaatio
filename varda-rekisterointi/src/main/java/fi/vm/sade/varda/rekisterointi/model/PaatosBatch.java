package fi.vm.sade.varda.rekisterointi.model;

import lombok.Value;

import java.util.List;

@Value
public class PaatosBatch {

    public final boolean hyvaksytty;
    public final String perustelu;
    public final List<Long> hakemukset;

}
