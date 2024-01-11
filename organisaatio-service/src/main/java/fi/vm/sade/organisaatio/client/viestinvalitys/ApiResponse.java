package fi.vm.sade.organisaatio.client.viestinvalitys;

import lombok.Data;

@Data
public class ApiResponse {
    private final Integer status;
    private final String body;
}
