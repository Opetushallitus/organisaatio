package fi.vm.sade.varda.rekisterointi.client.viestinvalitys;

import lombok.Data;

@Data
public class ApiResponse {
    private final Integer status;
    private final String body;
}
