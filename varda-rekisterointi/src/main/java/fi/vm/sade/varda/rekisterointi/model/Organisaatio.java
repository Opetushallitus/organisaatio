package fi.vm.sade.varda.rekisterointi.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.With;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;

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

    @NotNull @Column("alkupvm")
    public final LocalDate alkuPvm;

    @NotNull @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    public final KielistettyNimi ytjNimi;

    @NotNull
    public final String yritysmuoto;

    @NotNull
    public final Set<String> tyypit;

    @NotNull @Column("kotipaikka")
    public final String kotipaikkaUri;

    @NotNull @Column("maa")
    public final String maaUri;

    @NotNull @Column("kielet_uris")
    public final Set<String> kieletUris;

    @NotNull @Column("rekisterointi_id")
    public final Yhteystiedot yhteystiedot;

    @Setter
    @NotNull
    public boolean uudelleenRekisterointi;

    public boolean isKunta() {
        return tyypit != null && tyypit.contains("organisaatiotyyppi_09"); // kts. koodisto: organisaatiotyyppi
    }
}
