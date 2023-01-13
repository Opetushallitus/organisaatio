package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.dto.OrganisaatioNimiDTO;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioLiitosDTOV2;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioPerustietoV4;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioNimiRDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrganisaatioNimiMasking {
    private static final String[] YKSITYINEN_ELINKEINOHARJOITTAJA = {"Yksityinen elinkeinonharjoittaja", "Enskild näringsidkare", "Private trader"};
    private static final String[] HIDDEN_NAME = {"Piilotettu", "Dold", "Hidden"};

    @Autowired private OrganisaatioRepository organisaatioRepository;


    public void maskOrganisaatioPerustietoV4(OrganisaatioPerustietoV4 org) {
        if (org.isMaskingActive()) {
            org.setNimi(hideName(false, org.getYtunnus()));
            org.setLyhytNimi(hideName(false, org.getYtunnus()));
        }
    }

    public void maskOrganisaatioRDTOV4(OrganisaatioRDTOV4 org) {
        if (org.isMaskingActive()) {
            Map<String, String> maskedNimi = hideName(org.getPiilotettu(), Boolean.TRUE.equals(org.getPiilotettu()) ? org.getOid() : org.getYTunnus());
            org.setNimi(maskedNimi);
            org.setLyhytNimi(maskedNimi);
            List<OrganisaatioNimiRDTO> nimet = org.getNimet().stream().map(n -> {
                OrganisaatioNimiRDTO nimi = new OrganisaatioNimiRDTO();
                nimi.setOid(n.getOid());
                nimi.setNimi(maskedNimi);
                nimi.setAlkuPvm(n.getAlkuPvm());
                nimi.setPaivittaja(n.getPaivittaja());
                nimi.setVersion(n.getVersion());
                return nimi;
            }).collect(Collectors.toList());
            org.setNimet(nimet);
        }

    }

    public void maskOrganisaatioLiitosDTOV2(OrganisaatioLiitosDTOV2 liitos) {
        Organisaatio o = organisaatioRepository.findFirstByOid(liitos.getOrganisaatio().getOid());
        if (liitos.getOrganisaatio().isMaskingActive()) {
            liitos.getOrganisaatio().setNimi(hideName(o));
        }

        Organisaatio kohde = organisaatioRepository.findFirstByOid(liitos.getKohde().getOid());
        if (kohde.isMaskingActive()) {
            liitos.getKohde().setNimi(hideName(kohde));
        }
    }

    public void maskOrganisaatioNimiDTO(OrganisaatioNimiDTO nimi) {
        Organisaatio o = organisaatioRepository.findFirstByOid(nimi.getOid());
        if (o != null && o.isMaskingActive()) {
            nimi.setNimi(hideName(o));
        }
    }

    public Map<String, String> hideName(Organisaatio o) {
        return hideName(o.isPiilotettu(), o.isPiilotettu() ? o.getOid() : o.getYtunnus());
    }

    public Map<String, String> hideName(boolean piilotettu, String id) {
        String[] langNames = piilotettu ? HIDDEN_NAME : YKSITYINEN_ELINKEINOHARJOITTAJA;
        String nameFormat = "%s (%s)";
        return Map.of("fi", String.format(nameFormat, langNames[0], id),
                "sv", String.format(nameFormat, langNames[1], id),
                "en", String.format(nameFormat, langNames[2], id));
    }
}
