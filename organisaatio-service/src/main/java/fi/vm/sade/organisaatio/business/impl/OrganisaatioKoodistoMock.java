package fi.vm.sade.organisaatio.business.impl;


import fi.vm.sade.organisaatio.business.OrganisaatioKoodisto;
import fi.vm.sade.organisaatio.model.Organisaatio;

import java.util.Date;

public class OrganisaatioKoodistoMock implements OrganisaatioKoodisto {

    @Override
    public String paivitaKoodisto(Organisaatio entity, boolean reauthorize) {
        return null;
    }

    @Override
    public String lakkautaKoodi(String uri, String tunniste, Date lakkautusPvm, boolean reauthorize) {
        return null;
    }
}
