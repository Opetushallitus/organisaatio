package fi.vm.sade.organisaatio.resource.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ApiErrorDTO  {
    private final List<String> parameters;
    private final String errorMessage;
    private final String errorKey;
}
