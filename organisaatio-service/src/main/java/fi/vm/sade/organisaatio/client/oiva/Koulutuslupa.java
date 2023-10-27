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
        return loppupvm.map(date -> LocalDate.now().isBefore(date)).orElse(true);
    }
}
