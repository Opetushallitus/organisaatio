package fi.vm.sade.organisaatio.service.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.EntityManager;

import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.model.types.OsoiteDTO;
import fi.vm.sade.organisaatio.api.model.types.OsoiteTyyppi;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoArvoDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoDTO;
import fi.vm.sade.organisaatio.model.Organisaatio;

/**
* @author Antti
*/
public class OrganisaatioConverter extends Converter<OrganisaatioDTO, Organisaatio> {

    public OrganisaatioConverter(ConverterFactory converterFactory, EntityManager entityManager) {
        super(converterFactory, entityManager);
    }

    @Override
    public void setValuesToDTO(Organisaatio entity, OrganisaatioDTO dto) {
        setParentAndTyypit(dto, entity);
        setAllYhteystiedot(dto, entity);
        dto.getYhteystietoArvos().addAll(converterFactory.convertToDTO(entity.getYhteystietoArvos(), YhteystietoArvoDTO.class));//convertYhteystiedotToDTO(entity.getYhteystietoArvos(), YhteystietoArvoDTO.class));
        setVuosiluokat(dto, entity);
        setRyhmatyypit(dto, entity);
        setKayttoryhmat(dto, entity);
        dto.getKielet().addAll(entity.getKielet());
        convertNimiToDTO(entity, dto);
        //dto.setKuvailevatTiedot(new EntityToOrganisaatioKuvailevatTiedotTyyppiFunction(converterFactory).apply(entity.getMetadata()));
    }

    private void convertNimiToDTO(Organisaatio entity, OrganisaatioDTO dto) {
    	if (entity.getNimi() == null) {
    		return;
    	}
    	MonikielinenTekstiTyyppi nimiT = new MonikielinenTekstiTyyppi();
    	for (Entry<String,String> e : entity.getNimi().getValues().entrySet()) {
    		Teksti curTeksti = new Teksti();
    		curTeksti.setKieliKoodi(e.getKey());
    		curTeksti.setValue(e.getValue());
    		nimiT.getTeksti().add(curTeksti);
    	}
    	dto.setNimi(nimiT);
    }

    private void setVuosiluokat(OrganisaatioDTO dto, Organisaatio entity) {
        dto.getVuosiluokat().addAll(entity.getVuosiluokat());
    }

    private void setRyhmatyypit(OrganisaatioDTO dto, Organisaatio entity) {
        dto.getRyhmatyypit().addAll(entity.getRyhmatyypit());
    }

    private void setKayttoryhmat(OrganisaatioDTO dto, Organisaatio entity) {
        dto.getKayttoryhmat().addAll(entity.getKayttoryhmat());
    }

    private void setAllYhteystiedot(OrganisaatioDTO dto, Organisaatio entity) {
        List<OsoiteDTO> muutOs = new ArrayList<OsoiteDTO>();
        List<YhteystietoDTO> filteredYts = new ArrayList<YhteystietoDTO>();
        for (YhteystietoDTO curYt : converterFactory.convertToDTO(entity.getYhteystiedot(), YhteystietoDTO.class)) {
            if (curYt instanceof OsoiteDTO
                    && ((OsoiteDTO)curYt).getOsoiteTyyppi().equals(OsoiteTyyppi.MUU)) {
                muutOs.add((OsoiteDTO)curYt);
            } else {
                filteredYts.add(curYt);
            }
        }
        dto.getYhteystiedot().addAll(filteredYts);
        dto.getMuutOsoitteet().addAll(muutOs);
    }

    private void setParentAndTyypit(OrganisaatioDTO organisaatioDTO, Organisaatio organisaatio) {
        organisaatioDTO.setParentOid(organisaatio.getParent() != null ? organisaatio.getParent().getOid() : null);
        for (String ot : organisaatio.getTyypit()) {
            organisaatioDTO.getTyypit().add(OrganisaatioTyyppi.fromValue(ot));
        }
    }

}
