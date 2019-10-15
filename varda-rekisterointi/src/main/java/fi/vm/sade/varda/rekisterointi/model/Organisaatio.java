package fi.vm.sade.varda.rekisterointi.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
public class Organisaatio {

    @NotNull
    public final String ytunnus;

    public final String oid;

    @NotNull @Column("alkupvm")
    public final LocalDate alkuPvm;

    @NotNull @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    public final KielistettyNimi nimi;

    @NotNull
    public final String toimintamuoto;

    @NotNull
    public final String tyyppi;

    @NotNull @Column("kotipaikka")
    public final String kotipaikkaUri;

    @NotNull @Column("maa")
    public final String maaUri;

}
