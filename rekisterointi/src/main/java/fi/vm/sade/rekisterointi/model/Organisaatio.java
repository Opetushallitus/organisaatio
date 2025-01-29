package fi.vm.sade.rekisterointi.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.With;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
public class Organisaatio {

    @NotNull
    public final String ytunnus;

    @With
    public final String oid;

    @NotNull
    public final LocalDate alkuPvm;

    @NotNull
    public final KielistettyNimi ytjNimi;

    @NotNull
    public final String yritysmuoto;

    @NotNull
    public final Set<String> tyypit;

    @NotNull
    public final String kotipaikkaUri;

    @NotNull
    public final String maaUri;

    @NotNull
    public final Set<String> kieletUris;

    @NotNull
    public final Yhteystiedot yhteystiedot;

    @Setter
    @NotNull
    public boolean uudelleenRekisterointi;

    public boolean isKunta() {
        return tyypit != null && tyypit.contains("organisaatiotyyppi_09"); // kts. koodisto: organisaatiotyyppi
    }
}
