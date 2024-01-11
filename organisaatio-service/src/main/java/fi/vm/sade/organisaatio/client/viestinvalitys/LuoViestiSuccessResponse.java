package fi.vm.sade.organisaatio.client.viestinvalitys;

import lombok.Data;

@Data
public class LuoViestiSuccessResponse {
    private final String lahetysTunniste;
    private final String viestiTunniste;
}
