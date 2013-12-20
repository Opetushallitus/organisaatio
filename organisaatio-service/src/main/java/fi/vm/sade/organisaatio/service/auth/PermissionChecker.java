package fi.vm.sade.organisaatio.service.auth;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.auth.OrganisaatioContext;
import fi.vm.sade.organisaatio.auth.OrganisaatioPermissionServiceImpl;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAOImpl;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.service.NotAuthorizedException;
import fi.vm.sade.organisaatio.service.converter.MonikielinenTekstiTyyppiToEntityFunction;

/**
 * Encapsulate most of the auth check logic done at server here.
 */
@Component
public class PermissionChecker {

    @Autowired
    private OrganisaatioPermissionServiceImpl permissionService;

    @Autowired
    private OrganisaatioDAOImpl organisaatioDao;

    public void checkRemoveOrganisation(String oid) {
        checkPermission(permissionService
                .userCanDeleteOrganisation(OrganisaatioContext.get(oid)));
    }

    MonikielinenTekstiTyyppiToEntityFunction mkt2entity = new MonikielinenTekstiTyyppiToEntityFunction();

    public void checkSaveOrganisation(OrganisaatioDTO organisaatio,
            boolean update) {
        final OrganisaatioContext authContext = OrganisaatioContext
                .get(organisaatio);

        if (update) {
            final Organisaatio current = organisaatioDao.findByOid(organisaatio
                    .getOid());
            if (!Objects.equal(current.getNimi(),
                    mkt2entity.apply(organisaatio.getNimi()))) {

                // name changed
                checkPermission(permissionService.userCanEditName(authContext));
            }
            if(!(Objects.equal(organisaatio.getAlkuPvm(), current.getAlkuPvm()) && Objects.equal(organisaatio.getLakkautusPvm(), current.getLakkautusPvm()))) {
                // date(s) changed
                checkPermission(permissionService.userCanEditDates(authContext));
            }
            // TODO organisation type
            List<String> stringTyypit = Lists.newArrayList(Iterables.transform(
                    organisaatio.getTyypit(),

                    new Function<OrganisaatioTyyppi, String>() {
                        public String apply(OrganisaatioTyyppi input) {
                            return input.value();

                        }
                    }));
            if(!(stringTyypit.size()==current.getTyypit().size() && stringTyypit.containsAll(current.getTyypit()))){
                ///XXX what then?
            }
            checkPermission(permissionService.userCanUpdateOrganisation(authContext));
        } else {
            checkPermission(permissionService.userCanCreateOrganisation(OrganisaatioContext.get(organisaatio.getParentOid())));
            //TODO types
        }
    }

    private void checkPermission(boolean result) {
        if (!result) {
            throw new NotAuthorizedException("no.permission");
        }
    }

    public void checkEditYhteystietojentyyppi() {
        checkPermission(permissionService.userCanEditYhteystietojenTyypit());
    }

}
