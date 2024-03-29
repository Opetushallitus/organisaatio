package fi.vm.sade.organisaatio.client.oiva;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Data
public class Koulutuslupa {
    private String jarjestajaYtunnus;
    private LocalDate alkupvm;
    private Optional<LocalDate> loppupvm = Optional.empty();
    private List<String> koulutukset;
    private Optional<String> laajaOppisopimuskoulutus = Optional.empty();

    public boolean isVoimassaoleva() {
        var now = LocalDate.now();
        return loppupvm.map(loppu -> !now.isAfter(loppu)).orElse(true) && !now.isBefore(alkupvm);
    }
}
