package fi.vm.sade.organisaatio.resource.dto;

import com.fasterxml.jackson.annotation.JsonView;
import fi.vm.sade.organisaatio.api.views.Views;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Schema(description = "Hakutulos")
@JsonView(Views.Basic.class)
@Setter
@Getter
@Builder
public class HakuTulos<T> {
    @Schema(description = "Tulosjoukon koko")
    private int numHits;
    @Schema(description = "Tulokset")
    private Set<T> items;
}
