package fi.vm.sade.organisaatio.revised.ui.helper;

import fi.vm.sade.oid.service.ExceptionMessage;
import fi.vm.sade.oid.service.OIDService;
import fi.vm.sade.oid.service.types.NodeClassCode;
import fi.vm.sade.organisaatio.api.model.types.EmailDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OsoiteDTO;
import fi.vm.sade.organisaatio.api.model.types.PuhelinnumeroDTO;
import fi.vm.sade.organisaatio.api.model.types.WwwDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoArvoDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoDTO;

public class OidGenerator {
    
    public static void generateOids(OrganisaatioDTO organisaatio, OIDService oidService) throws ExceptionMessage {
        if (organisaatio.getOid() == null) {
            organisaatio.setOid(oidService.newOid(NodeClassCode.TOIMIPAIKAT));
        }
        for (YhteystietoDTO curYt : organisaatio.getYhteystiedot()) {
            if (curYt.getYhteystietoOid() == null) {
                curYt.setYhteystietoOid(oidService.newOid(NodeClassCode.TEKN_5));
            }
        }
        for (OsoiteDTO curOsoite : organisaatio.getMuutOsoitteet()) {
            if (curOsoite.getYhteystietoOid() == null) {
                curOsoite.setYhteystietoOid(oidService.newOid(NodeClassCode.TEKN_5));
            }
        }

        for (YhteystietoArvoDTO curYta : organisaatio.getYhteystietoArvos()) {
            if (curYta.getYhteystietoArvoOid() == null) {
                curYta.setYhteystietoArvoOid(oidService.newOid(NodeClassCode.TEKN_5));
            }
            if (((curYta.getArvo() instanceof OsoiteDTO)
                    || (curYta.getArvo() instanceof PuhelinnumeroDTO)
                    || (curYta.getArvo() instanceof WwwDTO)
                    || (curYta.getArvo() instanceof EmailDTO))
                    && (((YhteystietoDTO) curYta.getArvo()).getYhteystietoOid() == null)) {
                YhteystietoDTO ytArvo = (YhteystietoDTO) curYta.getArvo();
                ytArvo.setYhteystietoOid(oidService.newOid(NodeClassCode.TEKN_5));
            }
        }
    }
    
    public static void generateOid(YhteystietoDTO yhteystieto, OIDService oidService) throws ExceptionMessage {
        if (yhteystieto.getYhteystietoOid() == null) {
            yhteystieto.setYhteystietoOid(oidService.newOid(NodeClassCode.TEKN_5));
        }
    }
}
