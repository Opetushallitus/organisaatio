/*
*
* Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
*
* This program is free software:  Licensed under the EUPL, Version 1.1 or - as
* soon as they will be approved by the European Commission - subsequent versions
* of the EUPL (the "Licence");
*
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* European Union Public Licence for more details.
*/
package fi.vm.sade.organisaatio.repository.impl;

import fi.vm.sade.generic.dao.AbstractJpaDAOImpl;
import fi.vm.sade.organisaatio.repository.YtjVirheRepository;
import fi.vm.sade.organisaatio.model.YtjPaivitysLoki;
import fi.vm.sade.organisaatio.model.YtjVirhe;
import fi.vm.sade.organisaatio.repository.YtjVirheRepositoryCustom;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class YtjVirheRepositoryImpl implements YtjVirheRepositoryCustom {


    @Override
    public List<YtjVirhe> findOrganisaationPaivitysVirheet(String oid, YtjPaivitysLoki ytjPaivitysLoki) {
        return null; // TODO eh?
    }
}
