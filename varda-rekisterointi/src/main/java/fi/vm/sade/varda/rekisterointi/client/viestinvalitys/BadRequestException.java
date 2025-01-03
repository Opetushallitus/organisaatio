package fi.vm.sade.varda.rekisterointi.client.viestinvalitys;

import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {
    private final ApiResponse apiResponse;

    public BadRequestException(ApiResponse apiResponse) {
        super(apiResponse.toString());
        this.apiResponse = apiResponse;
    }
}
