package fi.vm.sade.organisaatio.resource.dto;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class HakutoimistoDTO {
    public final Map<String, String> nimi;
    public final Map<String, HakutoimistonYhteystiedotDTO> yhteystiedot;


    @ToString
    @EqualsAndHashCode
    @RequiredArgsConstructor
    public static class OsoiteDTO {
        public final String yhteystietoOid;
        public final String katuosoite;
        public final String postinumero;
        public final String postitoimipaikka;
    }

    @ToString
    @EqualsAndHashCode
    @RequiredArgsConstructor
    public static class HakutoimistonYhteystiedotDTO {
        public final OsoiteDTO kaynti;
        public final OsoiteDTO posti;
        public final String www;
        public final String email;
        public final String puhelin;
    }
}
