package fi.vm.sade.varda.rekisterointi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.relational.core.mapping.Column;

@AllArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE, onConstructor_ = @JsonCreator)
@Builder
public class Osoite {

    public static final Osoite TYHJA = Osoite.of("", "", "");

    public final String katuosoite;
    @Column("postinumero_uri")
    public final String postinumeroUri;
    public final String postitoimipaikka;

}
