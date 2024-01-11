package fi.vm.sade.organisaatio.client.viestinvalitys;

import lombok.Getter;

@Getter
public class UnexpectedResponseException extends RuntimeException {
    private final ApiResponse apiResponse;

    public UnexpectedResponseException(ApiResponse apiResponse) {
        super(apiResponse.toString());
        this.apiResponse = apiResponse;
    }
}

