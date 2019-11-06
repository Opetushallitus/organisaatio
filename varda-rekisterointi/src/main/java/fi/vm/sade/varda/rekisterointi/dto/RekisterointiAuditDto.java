package fi.vm.sade.varda.rekisterointi.dto;

import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;

public class RekisterointiAuditDto {

    public Rekisterointi.Tila tila;

    public RekisterointiAuditDto(Rekisterointi.Tila tila) {
        this.tila = tila;
    }
}
