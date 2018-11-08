package fi.vm.sade.organisaatio.service.converter;

import fi.vm.sade.organisaatio.api.model.types.YhteystietoArvoDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoElementtiTyyppi;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dao.YhteystietoElementtiDAO;
import fi.vm.sade.organisaatio.model.OrganisaatioBaseEntity;
import fi.vm.sade.organisaatio.model.YhteystietoArvo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;

public class YhteystietoArvoConverter extends Converter<YhteystietoArvoDTO, YhteystietoArvo> {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    public YhteystietoArvoConverter(ConverterFactory converterFactory, EntityManager entityManager) {
        super(converterFactory, entityManager);
    }

    @Override
    public void setValuesToDTO(YhteystietoArvo entity, YhteystietoArvoDTO dto) {
        dto.setOrganisaatioOid((entity.getOrganisaatio().getOid()));
        dto.setKenttaOid(entity.getKentta().getOid());
        Object theArvo = entity.getArvo();
        if (theArvo instanceof OrganisaatioBaseEntity) {
            theArvo = converterFactory.convertToDTO((OrganisaatioBaseEntity) theArvo);
        }
        dto.setArvo(theArvo);
        dto.setKieli(entity.getKieli());
    }

    @Override
    public void setValuesToJPA(YhteystietoArvoDTO dto, YhteystietoArvo entity, boolean merge, OrganisaatioDAO organisaatioDAO, YhteystietoElementtiDAO yhteistietoElementtiDAO) {

        LOG.info("setValuesToJPA({}, {}, {})", new Object[]{dto, entity, merge});
        if (entity != null) {
            LOG.info("  id=" + entity.getId());
        }

        if (dto.getOrganisaatioOid() != null) { // is null in insert, will be set when adding arvos to organisaatio.lisatietokentanArvos
            entity.setOrganisaatio(organisaatioDAO.findByOid(dto.getOrganisaatioOid())); //entityManager. find(Organisaatio.class, dto.getOrganisaatioId()));
        }
        entity.setKentta(yhteistietoElementtiDAO.findBy("oid", dto.getKenttaOid()).get(0));//entityManager.find(YhteystietoElementti.class, dto.getKenttaId()));
        Object theArvo = dto.getArvo();
        if (entity.getKentta().getTyyppi().equals(YhteystietoElementtiTyyppi.EMAIL.value())
                || entity.getKentta().getTyyppi().equals(YhteystietoElementtiTyyppi.WWW.value())
                || entity.getKentta().getTyyppi().equals(YhteystietoElementtiTyyppi.PUHELIN.value())
                || entity.getKentta().getTyyppi().equals(YhteystietoElementtiTyyppi.OSOITE.value())
                || entity.getKentta().getTyyppi().equals(YhteystietoElementtiTyyppi.OSOITE_ULKOMAA.value())) {
            entity.setArvoYhteystieto(converterFactory.convertYhteystietoToJPA((YhteystietoDTO) theArvo, true));
        } else if (entity.getKentta().getTyyppi().equals(YhteystietoElementtiTyyppi.TEKSTI.value())
                    || entity.getKentta().getTyyppi().equals(YhteystietoElementtiTyyppi.NIMI.value())
                    || entity.getKentta().getTyyppi().equals(YhteystietoElementtiTyyppi.NIMIKE.value())) {
            entity.setArvoText((String) theArvo);
            entity.setKieli(dto.getKieli());
        } else {
            throw new IllegalArgumentException("illegal arvo in YhteystietoArvo: "+theArvo);
        }

    }
}
